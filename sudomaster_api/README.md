# SudoMaster API

Spring Boot REST API for a Sudoku game application. Handles user authentication, real-time puzzle generation, move validation, scoring, leaderboards, and user preferences persistence.

**Base URL:** `http://localhost:8080`

---

## Tech Stack

- **Framework:** Spring Boot 3 (Java 21)
- **Database:** H2 in-memory (MySQL compatibility mode)
- **Auth:** Bearer Token (Base64-encoded `userId:timestamp`, 72-hour expiry)
- **Password:** BCrypt (strength 10)
- **Session:** Stateless (no server-side sessions)
- **Puzzle Gen:** Built-in algorithmic generator with difficulty evaluation (14 solving techniques)
- **Error Handling:** Global `@RestControllerAdvice` for validation and request errors
- **API Docs:** Springdoc OpenAPI (Swagger UI at `/swagger-ui/index.html`)

---

## Authentication

All game endpoints require a `Bearer` token in the `Authorization` header.

**Token format:** `Base64( userId + ":" + timestamp )`

The token is returned on register/login as `accessToken`. Include it in all authenticated requests:

```
Authorization: Bearer <accessToken>
```

**Token expiry:** 72 hours (259200000 ms). The `tokenExpiresIn` field in the auth response gives this value.

### Public endpoints (no auth required)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/leaderboard`
- `GET /api/leaderboard/all-time`
- `GET /swagger-ui/**`, `/v3/api-docs/**`
- `GET /h2/**` (H2 console)

### Protected endpoints (Bearer token required)
- All `/api/game/**` endpoints
- `POST /api/auth/logout`
- `DELETE /api/auth/delete-account`
- `POST /api/auth/deactivate-account`
- `PUT /api/auth/preferences`
- `GET /api/auth/preferences`
- `GET /api/user/stats`

**Unauthenticated requests** to protected endpoints return `401 Unauthorized` with `{"message": "Authentication required"}`.

---

## API Endpoints

### 1. Authentication (`/api/auth`)

#### `POST /api/auth/register`

Creates a new user account.

**Request:**
```json
{
  "email": "user@example.com",
  "username": "player1",
  "password": "mypassword123"
}
```

**Validation:**
- `email` — required, must be valid email format, unique
- `username` — required, 3–50 characters, unique
- `password` — required, 8–100 characters

**Response (201):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "player1",
  "email": "user@example.com",
  "accessToken": "NTUwZTg0MDAtZTI5Yi00MWQ0LWE3MTYtNDQ2NjU1NDQwMDAwOjE3MDcwMDAwMDAwMDA=",
  "tokenExpiresIn": 259200000,
  "tokenType": "Bearer",
  "message": "Registration successful",
  "preferencesJson": null
}
```

**Errors:**
- `400` — `"Email already registered"` or `"Username already taken"`

---

#### `POST /api/auth/login`

Authenticates a user by email or username.

**Request:**
```json
{
  "emailOrUsername": "player1",
  "password": "mypassword123"
}
```

**Validation:**
- `emailOrUsername` — required (accepts either email or username)
- `password` — required

**Response (200):** Same structure as register response, with `"message": "Login successful"` and the user's saved `preferencesJson`.

**Errors:**
- `401` — `"Invalid username or password"` (wrong credentials or inactive account)

---

#### `POST /api/auth/logout` (auth required)

Ends the user session. All IN_PROGRESS game sessions are preserved for later resumption.

**Request:** No body. Uses the authenticated user's ID from the Bearer token.

**Response (200):**
```
"Logout successful"
```

---

#### `DELETE /api/auth/delete-account` (auth required)

Permanently deletes the user account after password verification. Cascading delete removes: all game sessions, puzzles, scores, and user.

**Request:**
```json
{
  "password": "mypassword123"
}
```

**Response (200):**
```json
{
  "message": "Account deleted successfully"
}
```

**Errors:**
- `401` — `"Invalid password"` (password verification failed)

---

#### `POST /api/auth/deactivate-account` (auth required)

Temporarily deactivates the account (sets `isActive = false`). Preserves all data. Deactivated users cannot login.

**Request:** No body.

**Response (200):**
```json
{
  "message": "Account deactivated successfully"
}
```

---

#### `PUT /api/auth/preferences` (auth required)

Saves user preferences (color profile, font size, highlight settings) as a JSON string.

**Request:**
```json
{
  "preferencesJson": "{\"colorProfile\":\"blue\",\"fontSize\":\"medium\",\"highlightConflicts\":true}"
}
```

**Response (200):**
```json
{
  "preferencesJson": "{\"colorProfile\":\"blue\",\"fontSize\":\"medium\",\"highlightConflicts\":true}"
}
```

**Errors:**
- `400` — `"Preferences JSON is required"`
- `401` — Not authenticated

---

#### `GET /api/auth/preferences` (auth required)

Retrieves the authenticated user's preferences JSON.

**Response (200):**
```json
{
  "preferencesJson": "{\"colorProfile\":\"blue\",\"fontSize\":\"medium\"}"
}
```

Returns empty string if no preferences have been saved yet.

---

### 2. Game Operations (`/api/game`)

#### `POST /api/game/new` (auth required)

Creates a new Sudoku game with an algorithmically generated puzzle. Replaces any existing IN_PROGRESS session **for the same difficulty** (one active session per difficulty per user, up to 4 total).

**Request:**
```json
{
  "difficulty": "MEDIUM"
}
```

**Validation:**
- `difficulty` — required, must be one of: `EASY`, `MEDIUM`, `HARD`, `INSANE`

**Response (201):**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "puzzleId": 1,
  "cluesString": "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
  "boardString": "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
  "puzzle": [5,3,0,0,7,0,0,0,0,6,0,0,1,9,5,0,0,0,0,9,8,0,0,0,0,6,0],
  "candidates": {"0":[],"1":[],"80":[]},
  "difficulty": "MEDIUM",
  "status": "IN_PROGRESS",
  "createdAt": "2026-02-11T00:00:00",
  "updatedAt": "2026-02-11T00:00:00"
}
```

**Field descriptions:**
| Field | Type | Description |
|---|---|---|
| `sessionId` | string (UUID) | Unique game session identifier |
| `puzzleId` | integer | Database ID of the puzzle |
| `cluesString` | string (81 chars) | Original puzzle clues as flat string, 0 = empty cell |
| `boardString` | string (81 chars) | Current board state as flat string, 0 = empty cell |
| `puzzle` | integer[81] | Board state as array of 81 integers (for React) |
| `candidates` | object | Map of position to candidate list: `{"0": [1,3,7], "1": [], ...}` |
| `difficulty` | string | EASY / MEDIUM / HARD / INSANE |
| `status` | string | IN_PROGRESS / COMPLETED |
| `createdAt` | ISO datetime | Session creation time |
| `updatedAt` | ISO datetime | Last update time |

**Board format:** 81 characters read left-to-right, top-to-bottom. Position `i` maps to row `i/9`, column `i%9`. `0` = empty cell, `1-9` = filled cell.

---

#### `GET /api/game/{sessionId}` (auth required)

Retrieves the current state of a game session. Returns 404 if the session belongs to another user.

**Response (200):** Same `GameResponse` structure as create.

**Errors:**
- `403` — Access denied (session belongs to another user)
- `404` — Game not found

---

#### `POST /api/game/{sessionId}/move` (auth required)

Places or clears a value at a position on the board.

**Request:**
```json
{
  "position": 2,
  "value": 4,
  "candidates": []
}
```

**Validation:**
- `position` — required, integer 0–80
- `value` — required, integer 0–9 (0 = clear the cell)
- `candidates` — optional, list of integers (for frontend note-taking)

**Response (200):**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "boardString": "534070000600195000098000060800060003400803001700020006060000280000419005000080079",
  "candidates": {"0":[],"1":[]},
  "valid": true,
  "message": "Move successful",
  "moveId": null,
  "completionStatus": "IN_PROGRESS",
  "moveCount": 1
}
```

**Move validation rules:**
1. **Clue cell protection** — Cannot modify cells that contain original clues (`cluesString[position] != '0'`). Returns `valid: false`, message: `"Cannot modify prefilled cell"`.
2. **Duplicate detection** — Checks if placing the value creates a duplicate in the same row, column, or 3x3 box. Returns `valid: false`, message: `"Invalid move: Duplicate found"`.
3. **Value 0 (clear)** — Always valid, clears the cell.
4. **Puzzle completion** — If the board matches the solution after the move, message becomes: `"Congratulations! You completed the puzzle!"`.

**Important:** The move is always applied to the board even if `valid: false` (the duplicate is placed). The frontend should use the `valid` field to show warnings/errors to the user.

**Errors:**
- `400` — Invalid move
- `403` — Access denied
- `404` — Game not found
- `409` — Concurrent update conflict (optimistic locking)

---

#### `POST /api/game/{sessionId}/validate` (auth required)

Validates the current board state. Checks if the board has empty cells and whether it matches the solution.

**Response (200):**
```json
{
  "isValid": true,
  "isComplete": true,
  "errors": [],
  "status": "COMPLETED"
}
```

- `isValid: false` + `errors: ["Puzzle is not complete"]` — Board still has `0` cells
- `isComplete: true` — Board exactly matches solution string, status set to COMPLETED
- `isComplete: false` — Board doesn't match solution

---

#### `POST /api/game/{sessionId}/complete` (auth required)

Marks a game as completed, calculates the score, and records it on the leaderboard. Must be called by the frontend when the puzzle is solved.

**Request:**
```json
{
  "elapsedTime": 300,
  "mistakes": 2,
  "autoCandidateMode": false
}
```

**Validation:**
- `elapsedTime` — required, positive integer (seconds the player took)
- `mistakes` — required, non-negative integer (number of wrong placements tracked by frontend)
- `autoCandidateMode` — required, boolean (whether auto-candidate was enabled)

**Pre-conditions (checked server-side):**
1. Board must have no empty cells (`0`s)
2. Board must match the solution exactly
3. Session must not already have been scored

**Response (200):**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "difficulty": "MEDIUM",
  "elapsedTime": 300,
  "score": 4320,
  "completionStatus": "COMPLETED",
  "rank": 3,
  "message": "Congratulations! Your score: 4320 points"
}
```

**Score messages:**
- First completion at this difficulty: `"Congratulations! Your score: X points"`
- New personal best: `"New personal best! Score: X points"`
- Not a personal best: `"Good effort! Your best score is: X points"`

**Personal best logic:** Only ONE score record is kept per user per difficulty. If the new score is higher than the existing one, it overwrites it. If lower, the existing record is kept unchanged.

**After completion:** The game session and its associated puzzle are deleted (cleanup).

**Errors:**
- `400` — Board not solved, board incorrect, invalid parameters, already scored
- `409` — Concurrent update conflict (optimistic locking)

---

#### `POST /api/game/iscorrect` (auth required)

Checks if a user's answer for a specific cell is correct by comparing with the solution.

**Request:**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "row": 0,
  "column": 2,
  "value": 4
}
```

**Note:** The JSON field is `column` (mapped via `@JsonProperty`), but internally it's `col`.

**Validation:**
- `sessionId` — required
- `row` — required, integer 0–8
- `column` — required, integer 0–8
- `value` — required, integer 1–9

**Response (200):**
```json
{
  "correct": true,
  "message": "Correct answer!"
}
```

or

```json
{
  "correct": false,
  "message": "Incorrect answer."
}
```

**Errors:**
- `400` — Cell is a clue cell (`"This cell is a clue and cannot be modified"`)
- `403` — Session belongs to another user
- `404` — Session not found

---

### Session Persistence (`/api/game`)

#### `PUT /api/game/{sessionId}/save` (auth required)

Saves the current game state (timer, errors, candidates, board, settings) for later resumption.

**Request:**
```json
{
  "elapsedTimeSeconds": 120,
  "errorCount": 2,
  "autoCandidateModeUsed": true,
  "isAutoCandidateMode": false,
  "candidatesJson": "{\"0-1\":[3,5],\"2-4\":[1,7]}",
  "boardString": "534070000600195000098000060800060003400803001700020006060000280000419005000080079",
  "colorProfile": "blue",
  "settingsJson": "{\"highlightConflicts\":true,\"errorIndicator\":false,\"threeMistakeLimit\":false,\"fontSize\":\"medium\"}"
}
```

**Fields:**
- `elapsedTimeSeconds` — seconds played so far
- `errorCount` — mistake count tracked by frontend
- `autoCandidateModeUsed` — whether auto-candidate was used at any point during the game
- `isAutoCandidateMode` — whether auto-candidate mode is currently active
- `candidatesJson` — JSON string of candidate marks per cell
- `boardString` — current board state (81-char string)
- `colorProfile` — selected color theme name
- `settingsJson` — JSON string of game settings (highlights, error indicator, etc.)

**Response (200):**
```json
{
  "message": "Game saved successfully"
}
```

**Errors:**
- `403` — Session belongs to another user
- `404` — Game not found

---

#### `GET /api/game/saved?difficulty={difficulty}` (auth required)

Retrieves the user's saved IN_PROGRESS game session for a specific difficulty.

**Query parameters:**
- `difficulty` — required, one of: `EASY`, `MEDIUM`, `HARD`, `INSANE`

**Response (200):**
```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "cluesString": "530070000...",
  "boardString": "534070000...",
  "candidatesJson": "{\"0-1\":[3,5]}",
  "difficulty": "MEDIUM",
  "status": "IN_PROGRESS",
  "elapsedTimeSeconds": 120,
  "errorCount": 2,
  "autoCandidateModeUsed": true,
  "isAutoCandidateMode": false,
  "colorProfile": "blue",
  "settingsJson": "{\"highlightConflicts\":true}",
  "createdAt": "2026-02-11T00:00:00",
  "updatedAt": "2026-02-11T01:00:00"
}
```

**Response (204):** No saved game exists for that difficulty.

---

#### `DELETE /api/game/{sessionId}/abandon` (auth required)

Abandons (permanently deletes) a saved game session and its associated puzzle.

**Response (200):**
```json
{
  "message": "Game abandoned successfully"
}
```

---

### 3. Leaderboard & Stats (`/api/leaderboard`, `/api/user`)

#### `GET /api/leaderboard`

Retrieves top scores for a specific difficulty.

**Query parameters:**
| Param | Type | Default | Description |
|---|---|---|---|
| `difficulty` | string | `"EASY"` | EASY / MEDIUM / HARD / INSANE |
| `limit` | integer | `10` | Max number of results |

**Response (200):**
```json
[
  {
    "rank": 1,
    "username": "player1",
    "score": 8500,
    "elapsedTimeSeconds": 180,
    "mistakes": 0,
    "autoCandidateMode": false,
    "difficulty": "HARD",
    "completedAt": "2026-02-10T14:30:00"
  }
]
```

---

#### `GET /api/leaderboard/all-time`

Retrieves best scores across ALL difficulties.

**Query parameters:**
| Param | Type | Default | Description |
|---|---|---|---|
| `limit` | integer | `10` | Max number of results |

**Response:** Same structure as difficulty leaderboard.

---

#### `GET /api/user/stats` (auth required)

Retrieves the authenticated user's statistics and personal best scores.

**Response (200):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "player1",
  "email": "user@example.com",
  "totalGamesCompleted": 15,
  "bestScores": {
    "EASY": {
      "score": 1620,
      "elapsedTimeSeconds": 120,
      "mistakes": 0,
      "autoCandidateMode": false,
      "completedAt": "2026-02-10T10:00:00"
    },
    "MEDIUM": {
      "score": 4320,
      "elapsedTimeSeconds": 300,
      "mistakes": 2,
      "autoCandidateMode": false,
      "completedAt": "2026-02-10T12:00:00"
    }
  }
}
```

`bestScores` only includes difficulties where the user has completed at least one game.

---

## Scoring System

**Formula:** `score = basePoints * timeMultiplier * mistakePenalty * assistPenalty`

### Base Points

| Difficulty | Base Points |
|---|---|
| EASY | 1,000 |
| MEDIUM | 3,000 |
| HARD | 10,000 |
| INSANE | 25,000 |

### Time Multiplier

```
timeMultiplier = max(0.3, 1.8 - (elapsedMinutes / timeFactor))
```

| Difficulty | Time Factor (minutes) |
|---|---|
| EASY | 8 |
| MEDIUM | 15 |
| HARD | 30 |
| INSANE | 60 |

Minimum multiplier is `0.3` (very slow completion).

### Mistake Penalty

| Mistakes | Penalty |
|---|---|
| 0 | 1.00 (no penalty) |
| 1 | 0.95 |
| 2 | 0.90 |
| 3 | 0.80 |
| 4 | 0.65 |
| 5+ | 0.50 |

### Assist Penalty

| Auto-Candidate Mode | Penalty |
|---|---|
| OFF | 1.0 (no penalty) |
| ON | 0.6 (40% penalty) |

### Score Example

MEDIUM difficulty, 5 minutes, 1 mistake, auto-candidate off:
```
basePoints = 3000
timeMultiplier = max(0.3, 1.8 - (5/15)) = max(0.3, 1.467) = 1.467
mistakePenalty = 0.95
assistPenalty = 1.0
score = round(3000 * 1.467 * 0.95 * 1.0) = 4181
```

---

## Game Flow

### Typical game lifecycle

```
1. POST /api/auth/register       -> Get accessToken + preferencesJson
2. POST /api/game/new            -> Get sessionId, cluesString, boardString, puzzle[]
3. POST /api/game/{id}/move      -> Loop: place values, check valid flag
4. POST /api/game/iscorrect      -> Optional: check individual cell answers
5. POST /api/game/{id}/validate  -> Optional: check board validity
6. POST /api/game/{id}/complete  -> When solved: send elapsedTime, mistakes, autoCandidateMode
7. GET  /api/leaderboard         -> Show rankings
8. GET  /api/user/stats          -> Show personal bests
9. POST /api/auth/logout         -> End session (games preserved for resume)
```

### Save & Resume flow

```
1. PUT  /api/game/{id}/save              -> Save timer, errors, candidates, board, settings
2. POST /api/auth/logout                 -> Games preserved for next login
3. POST /api/auth/login                  -> Re-authenticate, get preferencesJson
4. GET  /api/game/saved?difficulty=MEDIUM -> Get saved game (or 204 if none)
5. Resume playing or DELETE /api/game/{id}/abandon -> Discard saved game
```

### Preferences flow

```
1. PUT  /api/auth/preferences  -> Save color profile, font size, settings
2. POST /api/auth/login        -> Returns saved preferences in auth response
3. GET  /api/auth/preferences  -> Retrieve preferences on demand
```

### Auto-cleanup behavior

- **Creating a new game** — Replaces the existing IN_PROGRESS session **for that difficulty only** (other difficulties are preserved)
- **Logout** — Preserves all IN_PROGRESS sessions for resumption on next login
- **Delete account** — Cascading delete of all user data (sessions, puzzles, scores)
- **Game completion** — Session and puzzle are deleted after scoring
- **Abandon** — Permanently deletes the specific session and puzzle
- **Stale session cleanup** — Scheduled task runs daily, removes sessions older than 3 days

### Board representation

The board is a flat 81-character string where each character is `0-9`:
- `0` = empty cell
- `1-9` = filled value
- Position `i` maps to: **row** = `Math.floor(i / 9)`, **col** = `i % 9`
- The `puzzle` array (Integer[81]) is the same data as an integer array for easier React consumption
- `cluesString` never changes — it's the original puzzle
- `boardString` changes with each move

### Candidates

The `candidates` field is a JSON object mapping cell position to a list of candidate values:
```json
{
  "0": [1, 3, 7],
  "1": [],
  "2": [2, 5],
  "80": []
}
```
Currently initialized as empty arrays. The frontend can use the `candidates` field in `MoveRequest` to send updated candidates, but the server stores them as JSON and returns them as-is.

---

## Data Model

### User
| Field | Type | Notes |
|---|---|---|
| id | string (UUID) | Auto-generated primary key |
| email | string | Unique |
| username | string | Unique |
| passwordHash | string | BCrypt encoded |
| isActive | boolean | Default: true |
| preferencesJson | text | JSON string for user preferences |
| createdAt | datetime | Auto-set on create |
| updatedAt | datetime | Auto-set on create/update |

### SudokuGameSession
| Field | Type | Notes |
|---|---|---|
| sessionId | string (UUID) | Primary key |
| user | User (FK) | many-to-one |
| puzzle | SudokuPuzzle (FK) | many-to-one |
| boardString | string (81) | Current board state |
| candidatesJson | text | JSON candidates map |
| elapsedTimeSeconds | int | Seconds played (for save/resume), default: 0 |
| errorCount | int | Mistake count (for save/resume), default: 0 |
| moveCount | int | Total moves made, default: 0 |
| autoCandidateModeUsed | boolean | Whether auto-candidate was used, default: false |
| isAutoCandidateMode | boolean | Current auto-candidate state, default: false |
| colorProfile | string (20) | Selected color theme, default: "orange" |
| settingsJson | text | JSON game settings |
| status | enum | IN_PROGRESS / COMPLETED |
| createdAt | datetime | |
| updatedAt | datetime | |
| version | long | Optimistic locking |

### SudokuPuzzle
| Field | Type | Notes |
|---|---|---|
| id | long | Auto-increment PK |
| cluesString | string (81) | Original puzzle |
| solutionString | string (81) | Correct solution |
| difficulty | enum | EASY / MEDIUM / HARD / INSANE |

### GameScore
| Field | Type | Notes |
|---|---|---|
| id | string (UUID) | Auto-generated PK |
| user | User (FK) | many-to-one |
| sessionId | string | Unique — one score per session |
| difficulty | enum | EASY / MEDIUM / HARD / INSANE |
| elapsedTimeSeconds | int | Time taken |
| score | int | Calculated score |
| mistakes | int | Mistake count |
| autoCandidateMode | boolean | Auto-candidate flag |
| completedAt | datetime | |
| createdAt | datetime | Auto-set on create |
| version | long | Optimistic locking |

---

## CORS Configuration

Allowed origins:
- `http://localhost:5173`
- `http://localhost:3000`
- `http://localhost:8000`
- `http://127.0.0.1:5173`
- `http://127.0.0.1:3000`
- `http://127.0.0.1:8000`

Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`

Credentials: **allowed** (`Access-Control-Allow-Credentials: true`)

---

## Running

```bash
./mvnw spring-boot:run
```

Server starts on `http://localhost:8080`. H2 console available at `http://localhost:8080/h2` (username: `sa`, no password).

### Build & Test

```bash
./mvnw clean compile    # Compile
./mvnw test             # Run tests
./mvnw package          # Build JAR
```

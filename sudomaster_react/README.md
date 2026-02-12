# Sudomaster81 — React Frontend

A fully-featured, responsive Sudoku web application built with **React 18** and **Vite 5**. Players register, log in, solve puzzles across four difficulty levels, track mistakes, compete on leaderboards, and customize their experience with color themes, font sizes, and game settings.

> **Backend required:** This frontend communicates with a Spring Boot REST API at `http://localhost:8080/api`. The backend handles puzzle generation, move validation, scoring, authentication, and leaderboard persistence.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Application Flow](#application-flow)
- [Authentication System](#authentication-system)
- [Game Flow](#game-flow)
- [API Endpoints](#api-endpoints)
- [Features](#features)
- [Keyboard Shortcuts](#keyboard-shortcuts)
- [Game Settings](#game-settings)
- [Color Profiles](#color-profiles)
- [State Management](#state-management)
- [Custom Hooks](#custom-hooks)
- [Component Architecture](#component-architecture)
- [Data Models](#data-models)
- [Error Handling](#error-handling)

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Framework | React | 18.3.1 |
| Build Tool | Vite | 5.4.x |
| UI Library | MUI (Material UI) | 7.3.7 |
| HTTP Client | Fetch API (native) | — |
| Styling | CSS Modules (plain CSS) | — |
| Linting | ESLint | 9.x |

### Dependencies

```
@emotion/react, @emotion/styled   — MUI styling engine
@mui/material, @mui/icons-material — UI components & icons
react, react-dom                   — Core framework
```

---

## Getting Started

### Prerequisites

- **Node.js** >= 18
- **npm** or **yarn**
- Spring Boot backend running on `http://localhost:8080`

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Opens on `http://localhost:5173` by default.

### Production Build

```bash
npm run build
npm run preview
```

---

## Project Structure

```
src/
├── App.jsx                        # Root component — routing by auth state
├── main.jsx                       # React DOM entry point
├── constants/
│   └── gameConstants.js           # Color profiles, dialog IDs, default settings
├── services/
│   ├── authService.js             # Unified facade for auth + game services
│   ├── userAuthService.js         # Register, login, logout, deleteAccount, preferences, token utilities
│   ├── gameService.js             # createGame, makeMove, completeGame, checkAnswer, saveGame, getSavedGame, abandonGame
│   ├── leaderboardService.js      # getLeaderboard, getUserProfile, getUserStats
│   └── httpClient.js              # fetchWithAuth (Bearer token injection), token management, server health check
├── hooks/
│   ├── useAppAuth.js              # App-level auth state machine
│   ├── useAppGame.js              # App-level game state (difficulty, started)
│   ├── useAppSettings.js          # Color profile, font size, about dialog, preferences persistence
│   ├── useGameBoard.js            # Board, candidates, selection, candidate modes
│   ├── useGameDialogs.js          # Dialog open/close/toggle state for 8 dialogs
│   ├── useGameHistory.js          # Undo stack with deep-copy snapshots
│   ├── useGameTimer.js            # Elapsed time, pause management, formatting
│   ├── useGameKeyboardInput.js    # Global keyboard event handler for game page
│   └── useDialogKeyboardShortcuts.js  # Enter/Escape handling for dialogs
├── utils/
│   ├── formatters.js              # formatTime(seconds) -> "MM:SS" or "H:MM:SS"
│   ├── sudokuLogic.js             # Candidate calculation, auto-candidate generation, conflict removal
│   └── validationHelpers.js       # Invalid cell detection, conflict finding
├── pages/
│   ├── GamePage/                  # Main game screen (board, number pad, all dialogs)
│   └── ServerErrorPage/           # Fullscreen error with retry button
└── components/
    ├── Auth/AuthForm.jsx          # Shared login/register form with validation
    ├── Login/Login.jsx            # Login-specific handler
    ├── Register/Register.jsx      # Register-specific handler with auto-login
    ├── DifficultySelector/        # Main menu with difficulty buttons, leaderboard, instructions, resume
    ├── Leaderboard/               # Leaderboard dialog with tabs per difficulty + player search
    ├── Footer/                    # App footer with copyright & about link
    ├── Common/SudokuIcon/         # SVG-based branding icon
    ├── Game/
    │   ├── Board/                 # 9x9 grid with highlighting logic
    │   ├── Cell/                  # Individual cell (value or candidate grid)
    │   ├── NumberPad/             # 1-9 input pad, Clear, Undo, mode toggles
    │   ├── GameTitle/             # Branded title banner
    │   └── GameInfo/              # Timer, mistakes, toolbar, mobile menu
    └── Dialogs/
        ├── PauseDialog/           # Pause overlay with resume
        ├── WinDialog/             # Victory screen with score
        ├── GameOverDialog/        # Three-mistake limit triggered — new game or exit
        ├── ConfirmDialog/         # Generic yes/no confirmation
        ├── SettingsDialog/        # Game settings, color profiles, auto-candidate toggle, delete account
        ├── InstructionsDialog/    # How to play guide
        ├── AboutDialog/           # App information & scoring details
        └── ResumeGameDialog/      # Resume saved game prompt per difficulty
```

---

## Application Flow

The app is a **single-page application** driven by an `authState` state machine in `App.jsx`:

```
┌─────────────┐     ┌─────────┐     ┌──────────┐     ┌──────┐     ┌──────┐
│  checking   │────>│  login   │<───>│ register │     │ menu │────>│ game │
│ (server OK?)│     │          │     │          │     │      │     │      │
└─────────────┘     └────┬─────┘     └──────────┘     └──┬───┘     └──┬───┘
       │                 │                                │           │
       │ server down     │ success                        │ logout    │ exit/logout
       v                 v                                v           v
┌─────────────┐     ┌──────┐                          ┌─────────┐
│ ServerError │     │ menu │                          │  login  │
└─────────────┘     └──────┘                          └─────────┘
```

### State Transitions

| Current State | Event | Next State |
|---|---|---|
| `checking` | Server healthy + token valid | `menu` |
| `checking` | Server healthy + no token | `login` |
| `checking` | Server unreachable | `ServerErrorPage` |
| `login` | Successful login | `menu` |
| `login` | Switch to register | `register` |
| `register` | Successful register + auto-login | `menu` |
| `register` | Switch to login | `login` |
| `menu` | Select difficulty (no saved game) | `game` |
| `menu` | Select difficulty (saved game exists) | `ResumeGameDialog` |
| `menu` | Resume saved game | `game` (with restored state) |
| `menu` | Discard saved game | `game` (new puzzle) |
| `menu` | Logout | `login` |
| `game` | Exit (back to menu) | `menu` (game auto-saved) |
| `game` | Logout | `login` |
| `game` | Server error (5xx / network) | `ServerErrorPage` |

---

## Authentication System

### Token Storage

Authentication data is stored in `localStorage`:

| Key | Value | Description |
|---|---|---|
| `token` | Bearer token string | Base64-encoded token for API requests |
| `tokenExpiry` | Unix timestamp (ms) | Client-side expiry check |
| `userId` | Numeric ID | User identifier |
| `username` | String | Display name |
| `userPreferences` | JSON string | User preferences (color profile, font size, settings) |

### Token Lifecycle

- **Default TTL:** 72 hours (from `tokenExpiresIn` or fallback)
- **Client-side validation:** Before every API request, `fetchWithAuth` checks `Date.now() < tokenExpiry`
- **Expired token:** Automatically clears localStorage and redirects to login
- **401 response:** Clears auth data (except for password-related errors like delete account)
- **Cross-tab sync:** Listens for `storage` events — if `token` is removed in another tab, redirects to login

### Registration Flow

1. `POST /auth/register` with `{ username, email, password }`
2. Server returns `{ accessToken, tokenExpiresIn, userId, username, preferencesJson }`
3. Token stored in localStorage
4. **Auto-login after 1.5s delay:** Automatically calls `POST /auth/login` to ensure fresh session
5. On auto-login failure: shows success message + switches to login form after 3s

### Login Validation (Client-side)

| Field | Rule |
|---|---|
| Username | Required |
| Password | Required |

### Registration Validation (Client-side)

| Field | Rule |
|---|---|
| Email | Required, valid email format |
| Username | Required, 3–16 characters |
| Password | Required, 8–22 characters |
| Confirm Password | Must match password |

---

## Game Flow

### 1. Puzzle Creation

```
Player selects difficulty -> POST /game/new -> Receive boardString (81-char) + sessionId + candidates
```

The `boardString` is an 81-character string where `0` represents empty cells. The frontend converts it to a 9x9 grid array.

### 2. Playing

- Player clicks a cell -> cell is selected (highlighted with row/column/box)
- Player clicks a number (1–9) on the NumberPad or presses a key
- **Normal mode:** Places the number in the cell -> sends `POST /game/{sessionId}/move`
- **Candidate mode:** Toggles pencil marks locally (not sent to server)
- **Auto-candidate mode:** Automatically calculates and displays all valid candidates; updates when numbers are placed

### 3. Answer Validation (Optional)

When **Error Indicator** is enabled in settings:
- Before placing a number, sends `POST /game/iscorrect` to verify
- If incorrect: increments mistake counter (with shake animation)
- If correct: proceeds normally

### 4. Conflict Detection (Client-side)

When a number is placed, the frontend checks for Sudoku rule violations:
- Same number in the same row
- Same number in the same column
- Same number in the same 3x3 box
- Conflicting cells are visually marked as invalid (red)

### 5. Win Detection

Two detection methods:
1. **Server-side:** If `POST /game/{sessionId}/move` returns `{ completionStatus: 'COMPLETED' }`, the game is won
2. **Client-side fallback:** If all 81 cells are filled, triggers win

### 6. Game Completion

```
Win detected -> POST /game/{sessionId}/complete -> Receive { score, elapsedTime }
```

Completion data sent:
```json
{
  "elapsedTime": 245,
  "mistakes": 3,
  "autoCandidateMode": false
}
```

### 7. Three-Mistake Limit (Optional)

When the **Three Mistake Limit** setting is enabled:
- Each wrong move (validated by `POST /game/iscorrect`) increments the error counter
- Mistake display shows `errorCount / 3`
- When `errorCount >= 3`, a **GameOverDialog** appears with options to start a new game or exit to menu
- If the setting is toggled on mid-game and errors are already >= 3, game over triggers immediately

### 8. Undo System

- Full history stack with deep-copy snapshots of board + candidates + selected cell + auto-candidate mode
- Each number placement or candidate toggle creates a snapshot
- Undo restores the previous snapshot and recalculates invalid cells

### 9. Save & Resume

- Game state is auto-saved on exit (back to menu) and preserved on logout
- Each difficulty has an independent saved game slot (up to 4 total)
- On selecting a difficulty with a saved game, a **ResumeGameDialog** appears:
  - **Continue** — resume with full state (board, timer, candidates, errors, settings)
  - **New Game** — abandon saved game and start fresh
  - **Cancel** — return to difficulty selection
- Saved data includes: `boardString`, `candidatesJson`, `elapsedTimeSeconds`, `errorCount`, `autoCandidateModeUsed`, `isAutoCandidateMode`, `settingsJson`

### 10. Preferences Persistence

User preferences (color profile, font size, game settings) are persisted server-side:
- **Save:** `PUT /auth/preferences` with JSON settings (triggered on changes)
- **Load:** On login, `preferencesJson` is returned in the auth response and stored in localStorage
- **Retrieve:** `GET /auth/preferences` for on-demand loading
- **Merge:** `useAppSettings` merges saved preferences with defaults, falling back to localStorage then defaults

---

## API Endpoints

All endpoints use base URL: `http://localhost:8080/api`

### Authentication

| Method | Endpoint | Body | Response | Auth |
|---|---|---|---|---|
| `POST` | `/auth/register` | `{ username, email, password }` | `{ accessToken, tokenExpiresIn, userId, username, preferencesJson }` | No |
| `POST` | `/auth/login` | `{ emailOrUsername, password }` | `{ accessToken, tokenExpiresIn, userId, username, preferencesJson }` | No |
| `POST` | `/auth/logout` | — | — | Yes |
| `DELETE` | `/auth/delete-account` | `{ password }` | — | Yes |
| `PUT` | `/auth/preferences` | `{ preferencesJson }` | `{ preferencesJson }` | Yes |
| `GET` | `/auth/preferences` | — | `{ preferencesJson }` | Yes |
| `OPTIONS` | `/auth/login` | — | (health check) | No |

### Game

| Method | Endpoint | Body | Response | Auth |
|---|---|---|---|---|
| `POST` | `/game/new` | `{ difficulty }` | `{ sessionId, boardString, candidates, ... }` | Yes |
| `POST` | `/game/{sessionId}/move` | `{ position, value }` | `{ completionStatus, valid, moveCount, ... }` | Yes |
| `POST` | `/game/{sessionId}/complete` | `{ elapsedTime, mistakes, autoCandidateMode }` | `{ score, rank, message, ... }` | Yes |
| `POST` | `/game/iscorrect` | `{ sessionId, row, column, value }` | `{ correct, message }` | Yes |
| `PUT` | `/game/{sessionId}/save` | `{ elapsedTimeSeconds, errorCount, candidatesJson, boardString, settingsJson, ... }` | `{ message }` | Yes |
| `GET` | `/game/saved?difficulty=X` | — | `SavedGameResponse` or 204 | Yes |
| `DELETE` | `/game/{sessionId}/abandon` | — | `{ message }` | Yes |

### Leaderboard & User

| Method | Endpoint | Query Params | Response | Auth |
|---|---|---|---|---|
| `GET` | `/leaderboard` | `difficulty`, `limit` | `[...entries]` | Yes |
| `GET` | `/user/stats` | — | User statistics object | Yes |

### Request Format

All authenticated requests include:
```
Headers:
  Content-Type: application/json
  Authorization: Bearer <TOKEN>
```

### Error Response Format

```json
{
  "message": "Error description"
}
```

---

## Features

### Core Gameplay
- **4 difficulty levels:** Easy, Medium, Hard, Insane
- **Real-time conflict highlighting:** Duplicate numbers in row, column, or box shown in red
- **Candidate/Pencil marks:** Toggle per-cell candidate numbers (manual mode)
- **Auto-candidate mode:** Automatically calculates all valid candidates for empty cells; updates dynamically as numbers are placed or cleared. Toggleable from Settings or NumberPad. Affects scoring (40% penalty).
- **Undo:** Full history stack — undo any number of moves back to initial state
- **Clear cell:** Remove placed number or all candidates from selected cell
- **Timer:** Live elapsed time counter, pauses when any dialog is open or game is won
- **Error indicator:** Optional mistake counter validated against the server solution
- **Three-mistake limit:** Optional game over mode — when enabled, 3 wrong answers ends the game with a GameOver dialog
- **Font size:** Adjustable board font size (small / medium / large) via Settings
- **Win detection:** Server-validated + client-side fallback for board completion
- **Save & Resume:** Game state auto-saved on exit/logout — resume per difficulty with full board, candidates, timer, errors, and settings preserved

### Navigation & UI
- **Responsive design:** Full desktop layout with sidebar info; mobile layout with hamburger menu and compact controls
- **Keyboard navigation:** Arrow keys to move between cells, number keys to input
- **Cell highlighting:** Configurable highlighting for row/column, 3x3 box, and identical numbers
- **Loading states:** Fullscreen backdrop with spinner during puzzle generation and data loading
- **Error states:** Inline error messages, snackbar notifications, dedicated server error page

### Leaderboard
- **Per-difficulty tabs:** Easy, Medium, Hard, Insane
- **Top 50 entries** per difficulty
- **Player search:** Search across all difficulties by username
- **Current player highlighting:** The logged-in user's entries are visually highlighted
- **Score display:** Rank, username, score, time, difficulty, date

### Account Management
- **Registration** with email, username (3–16 chars), password (8–22 chars)
- **Login** with username or email
- **Logout** with confirmation dialog
- **Delete account** with password confirmation (from Settings dialog)
- **Auto-logout** on token expiry or revocation
- **Preferences sync** — color profile, font size, and game settings persisted server-side

### Dialogs (9 total)
| Dialog | Purpose |
|---|---|
| **PauseDialog** | Pause game and resume |
| **WinDialog** | Display score, time, and difficulty on win |
| **GameOverDialog** | Three-mistake limit reached — new game or exit |
| **ConfirmDialog** | Generic confirmation for exit/logout |
| **SettingsDialog** | Game settings, color themes, auto-candidate toggle, delete account |
| **InstructionsDialog** | How to play guide |
| **AboutDialog** | App information, scoring system, algorithm details |
| **Leaderboard** | View and search leaderboard rankings |
| **ResumeGameDialog** | Prompt to resume or discard a saved game (per difficulty) |

---

## Keyboard Shortcuts

### During Gameplay (no dialog open)

| Key | Action |
|---|---|
| `1`–`9` | Place number / toggle candidate |
| `Delete` / `Backspace` | Clear selected cell |
| `Shift` / `Tab` | Toggle candidate mode |
| `Arrow Keys` | Navigate between cells |
| `Ctrl+Z` / `Cmd+Z` | Undo |
| `Space` | Pause / Resume |
| `R` | Restart puzzle |
| `M` | Exit to main menu |
| `L` | Logout |
| `S` | Open Settings |
| `I` | Open Instructions |
| `A` | Open About |
| `Escape` | Open exit dialog / close current dialog |
| `Enter` | Confirm current dialog |

### In Dialogs

| Key | Action |
|---|---|
| `Escape` | Close / Cancel |
| `Enter` | Confirm / OK |
| `Space` | Resume (Pause dialog only) |

### Difficulty Selector Screen

| Key | Action |
|---|---|
| `Escape` | Open logout dialog |
| `Enter` | Confirm logout |
| `I` | Open instructions |
| `A` | Open about |

---

## Game Settings

Configurable through the **Settings Dialog** during gameplay:

| Setting | Default | Description |
|---|---|---|
| Highlight Conflicts | `true` | Visually mark cells that violate Sudoku rules |
| Highlight Row & Column | `true` | Highlight the row and column of the selected cell |
| Highlight Box | `true` | Highlight the 3x3 box of the selected cell |
| Highlight Identical Numbers | `true` | Highlight all cells with the same number as selected |
| Error Indicator | `false` | Enable server-validated mistake counting |
| Three Mistake Limit | `false` | End game after 3 wrong answers (requires Error Indicator) |
| Font Size | `'medium'` | Board cell font size: `small`, `medium`, or `large` |
| Auto-Candidate Mode | toggle | Calculate and display all valid candidates automatically (affects score: 40% penalty) |
| Color Profile | `'orange'` | Select from 7 color themes (see below) |

---

## Color Profiles

7 selectable color themes that affect board highlighting, UI accents, and cell backgrounds:

| Profile | Intensive Color | Light Color |
|---|---|---|
| **Orange** (default) | `#ffa500` | `#f5f5dc` |
| **Lemon** | `#f0e68c` | `#fffef0` |
| **Green** | `#a0c98a` | `#e8f4e1` |
| **Blue** | `#6ba3d4` | `#e8f2fa` |
| **Pink** | `#e8a5c8` | `#f9e8f5` |
| **Purple** | `#b8a3d4` | `#f0e8f8` |
| **Lavender** | `#dda0dd` | `#f8f0ff` |

Colors are passed as CSS custom properties (`--color-intensive`, `--color-light`) through the component tree.

---

## State Management

The app uses **React hooks** exclusively — no external state management library (Redux, Zustand, etc.).

### App-Level State (App.jsx)

| Hook | Manages |
|---|---|
| `useAppAuth` | Auth state machine (`checking` -> `login` -> `menu` -> `game`), user object, server error flag |
| `useAppGame` | Game started flag, selected difficulty |
| `useAppSettings` | Color profile, font size, about dialog visibility, preferences persistence (server + localStorage) |

### Game-Level State (GamePage.jsx)

| Hook | Manages |
|---|---|
| `useGameBoard` | 9x9 board array, original board, candidates map, selected cell, candidate modes |
| `useGameDialogs` | Open/close state for 8 dialogs, `anyDialogOpen` computed flag |
| `useGameHistory` | Undo stack of board/candidate snapshots |
| `useGameTimer` | Elapsed time, pause state, refs for accurate timing |
| `useGameKeyboardInput` | Global keydown listener for game interactions |

### Local Component State

| State | Location | Description |
|---|---|---|
| `sessionId` | GamePage | Current game session identifier |
| `isGameWon` | GamePage | Win flag |
| `errorCount` | GamePage | Mistake counter (when error indicator enabled) |
| `settings` | GamePage | Current game settings object |
| `invalidCells` | GamePage | Set of cell keys (`"row-col"`) with conflicts |
| `winTime`, `winScore` | GamePage | Final time and score from server |
| `message` | GamePage | Temporary error/info messages |

### Concurrency Guards

| Ref | Purpose |
|---|---|
| `pendingMoveRef` | Prevents concurrent move API calls |
| `pendingCheckAnswerRef` | Prevents concurrent answer-check API calls |
| `completionSentRef` | Prevents duplicate game completion submissions |
| `gameInitializedRef` | Ensures single initialization per mount |
| `pendingTimersRef` | Tracks all `setTimeout` IDs for cleanup on unmount |

---

## Custom Hooks

### `useAppAuth()`
Manages the authentication state machine. Performs initial server health check and token validation on mount. Handles login/register/logout transitions and server error detection.

### `useAppGame(onAuthStateChange)`
Tracks whether a game is active and its difficulty level. Calls `onAuthStateChange` to sync auth state on game start/exit.

### `useAppSettings()`
Manages persistent color profile and font size selection. Loads preferences from `userPreferences` in localStorage (populated on login from server), merges with defaults, and saves changes back to server via `PUT /auth/preferences`. Also handles about dialog visibility.

### `useGameBoard()`
Provides board state (`board`, `originalBoard`, `candidates`, `selectedCell`), candidate mode flags, and a `resetBoard()` function.

### `useGameDialogs(ref)`
Manages open/close state for 8 dialogs. Exposes `useImperativeHandle` for parent components to programmatically open the About dialog.

### `useGameHistory(initialBoard, initialCandidates)`
Maintains an undo stack of deep-copied snapshots. Each snapshot contains: `board`, `candidates`, `selectedCell`, `isAutoCandidateMode`. Supports `saveToHistory()`, `undo()`, `resetHistory()`, and `canUndo()`.

### `useGameTimer()`
Provides `elapsedTime` state, `isPaused` flag, refs for accurate pause tracking (`pauseStartTimeRef`, `elapsedTimeRef`), and `formatElapsedTime()` wrapper.

### `useGameKeyboardInput(config)`
Global `keydown` event listener that handles: number input, cell navigation, undo, dialog shortcuts, pause, and mode toggles. Automatically cleans up on unmount.

### `useDialogKeyboardShortcuts({ onConfirm, onCancel, onClose })`
Reusable Enter/Escape handlers for dialogs.

---

## Component Architecture

```
App
├── ServerErrorPage                    (when server unreachable)
├── Login -> AuthForm                  (login screen)
├── Register -> AuthForm               (register screen)
├── DifficultySelector                 (main menu)
│   ├── Leaderboard                    (dialog)
│   ├── InstructionsDialog             (dialog)
│   ├── AboutDialog                    (dialog)
│   ├── ConfirmDialog                  (logout confirmation)
│   └── ResumeGameDialog               (resume/discard saved game per difficulty)
├── GamePage                           (main game)
│   ├── GameTitle                      (header branding)
│   ├── GameInfo
│   │   ├── GameInfoHeader             (timer, difficulty, mistakes)
│   │   ├── GameInfoToolbar            (desktop action buttons)
│   │   └── MobileGameMenu             (hamburger menu for mobile)
│   ├── Board
│   │   └── Cell (x81)                 (individual cell with value/candidates)
│   ├── NumberPad                      (1-9 input, clear, undo, mode toggle)
│   ├── PauseDialog
│   ├── WinDialog
│   ├── GameOverDialog                 (three-mistake limit)
│   ├── ConfirmDialog (x2)             (exit + logout)
│   ├── SettingsDialog
│   ├── InstructionsDialog
│   └── AboutDialog
└── Footer                             (copyright + about link)
```

---

## Data Models

### Board Representation

```javascript
board[row][col]  // 9x9 array, values are "" (empty) or "1"-"9" (string)
```

### Candidates Map

```javascript
candidates["row-col"]  // Array of numbers, e.g. [1, 3, 7]
```

### Invalid Cells

```javascript
invalidCells  // Set of strings, e.g. Set {"0-3", "4-3", "0-7"}
```

### History Snapshot

```javascript
{
  board: [/* 9x9 deep copy */],
  candidates: { "0-0": [1,3], "0-1": [2,5,9], ... },
  selectedCell: { row: 3, col: 5 } | null,
  isAutoCandidateMode: false
}
```

### Auth Data (localStorage)

```javascript
{
  token: "NTUwZTg0MDAtZTI5Yi00MWQ0...",
  tokenExpiry: "1739456789000",
  userId: "42",
  username: "player1",
  userPreferences: "{\"colorProfile\":\"blue\",\"fontSize\":\"medium\"}"
}
```

### Game Creation Response

```javascript
{
  sessionId: "abc-123-def",
  boardString: "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
  candidates: { "0": [1,2], "2": [1,4,9], ... }  // keyed by cell index (0-80)
}
```

### Move Request

```javascript
{
  position: 19,     // row * 9 + col
  value: 5          // 1-9 or 0 to clear
}
```

### Completion Request

```javascript
{
  elapsedTime: 245,           // seconds
  mistakes: 3,                // error count
  autoCandidateMode: false    // whether auto-candidates were used
}
```

### Answer Check Request/Response

```javascript
// Request
{ sessionId: "abc-123", row: 2, column: 5, value: 7 }

// Response
{ correct: true, message: "Correct answer!" }
```

### Save Game Request

```javascript
{
  elapsedTimeSeconds: 120,
  errorCount: 2,
  autoCandidateModeUsed: true,
  isAutoCandidateMode: false,
  candidatesJson: "{\"0-1\":[3,5],\"2-4\":[1,7]}",
  boardString: "534070000...",
  settingsJson: "{\"highlightConflicts\":true,\"errorIndicator\":false}"
}
```

---

## Error Handling

### Network Errors
- Server unreachable -> `ServerErrorPage` with retry button
- Request timeout (health check: 5s) -> Server error state

### Authentication Errors
- **401:** Clears auth data, redirects to login (except for password-validation errors)
- **403:** Throws error with server message
- **Token expired (client-side):** Clears auth data pre-request

### Game Errors
- Failed puzzle creation -> Inline error with retry button
- Failed move -> Temporary error message banner (3s)
- Failed score save -> Silently caught (game still shows as won)

### Validation Errors
- Client-side form validation with snackbar notifications
- Server-side validation errors surfaced through error messages

---

## License

(c) 2026 [Vasef97](https://github.com/Vasef97)

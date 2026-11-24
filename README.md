# Poker

A desktop Texas Hold'em poker game written in Java Swing. Play as the human seat against 1–6 computer opponents (2–7 total seats). The project uses only the Java standard library; no external dependencies or build tool are required.

## Features

- Texas Hold'em dealing with a shuffled 52-card deck
- Pre-flop, flop, turn, and river betting rounds
- Fold, check/call, and raise actions
- Computer-controlled opponents with basic hand-strength-based decisions
- Hand ranking from high card through royal flush, including kicker comparisons
- Showdown evaluation, split pots, odd-chip handling, and side pots for all-in players
- Chip stacks, player status, community cards, and card/chip artwork
- A plain-Java test suite for poker hand evaluation and pot distribution

## Requirements

- JDK 17 or newer (JDK 21 is recommended)
- A graphical desktop environment for the Swing application

Check your Java installation:

```bash
java -version
javac -version
```

## Run the game

Run these commands from the repository root.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\*.java
java -cp "out;src" App
```

### macOS / Linux

```bash
mkdir -p out
javac -d out src/*.java
java -cp out:src App
```

The `src` directory must remain on the runtime classpath because it contains the card and chip images loaded as classpath resources.

## Run the tests

The test suite does not require JUnit.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out src\*.java test\*.java
java -cp "out;src" PokerLogicTest
```

### macOS / Linux

```bash
mkdir -p out
javac -d out src/*.java test/*.java
java -cp out:src PokerLogicTest
```

A successful run ends with `N passed, 0 failed`. The evaluator tests also compare 100,000 random showdowns against an independent five-card reference evaluator, and the pot tests check conservation over 50,000 randomized contribution sets.

## How to play

1. Launch the application and select 2–7 total players.
2. Select **Start** to deal a hand. Each player antes 5 chips.
3. When it is your turn, choose **Fold**, **Check/Call**, or enter a raise amount and choose **Raise**.
4. Use **Next** to let the computer opponents act until it is your turn again.
5. The board reveals the flop (three cards), turn (one card), and river (one card). The best five-card hand wins at showdown; if everyone else folds, the remaining player wins immediately.
6. Select **New Game** to deal the next hand. The first-to-act position rotates between hands. The game ends when you run out of chips or eliminate all opponents.

Raise amounts are the additional amount above the call. A raise must be at least 5 chips unless it is an all-in raise. Up to four raises are allowed per betting round.

## Project structure

```text
Poker-main/
├── docs/                 # Example screenshots
├── src/
│   ├── App.java           # Swing interface and game flow
│   ├── Board.java         # Community cards and current bet
│   ├── Deck.java          # 52-card deck and shuffle
│   ├── HandEvaluator.java # Hand ranking and tie breakers
│   ├── Images.java        # Card/chip image resource loading
│   ├── Player.java        # Player state, cards, and chip actions
│   ├── Pots.java          # Main/side-pot distribution
│   ├── cards/             # Card artwork
│   └── chips/             # Chip artwork
└── test/
    └── PokerLogicTest.java
```

## Notes / limitations

- This is a local, single-device game; it does not include online multiplayer, accounts, persistence, or real-money wagering.
- Computer opponents use a lightweight heuristic, not a full poker strategy or odds calculator.
- Game state resets when the application is closed.

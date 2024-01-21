
# **The Memory Game**

## **Description:**
The Memory Game is an engaging and interactive Android application that challenges players to match pairs of images. It combines intuitive gameplay with visually appealing graphics and responsive user interfaces to create a fun and memorable gaming experience.

## **Key Features:**
1. **Dynamic Image Fetching:** The game fetches images from a specified URL, allowing for a unique experience each time. Players can input their desired URL to customize the set of images used in the game.

2. **Gameplay Mechanics:** Players select pairs of images to find matches. The game includes a grid of images that players interact with, flipping cards to reveal the images.

3. **Difficulty Modes:** Includes a 'Hard Mode' for an increased challenge, adding an extra layer of complexity and enjoyment for players seeking a more demanding experience.

4. **Timer Functionality:** A game timer adds urgency to the gameplay, challenging players to make matches within the allocated time.

5. **Audio Feedback:** Incorporates sound effects for correct and incorrect matches, enhancing player engagement and providing immediate feedback on actions.

6. **End-Game Dialog:** At the end of each game, a dialog appears displaying the result (win/lose) and offering options to restart the game or exit.

## **Technologies Used:**
- Android Studio for development
- Java for core game logic
- XML for layout design
- Picasso library for image loading
- Jsoup library for HTML parsing
- Android SDK for UI components

## **Game Flow:**
1. The `MainActivity` handles the initial user interface where players enter the URL to fetch images.
2. The `CardMatchActivity` manages the core gameplay, handling the logic for flipping cards, checking matches, and tracking game progress.
3. Utilizes various utility classes (`GameUtils`, `ImageDownloader`, etc.) to manage game functions like image downloading and timer management.

## **Custom Views and Animations:**
- Custom card views for displaying images.
- Animations for flipping cards and indicating match results.

## **Installation and Running:**
- Clone the repository and import the project into Android Studio.
- Ensure all dependencies are resolved.
- Run the application on an Android device or emulator.

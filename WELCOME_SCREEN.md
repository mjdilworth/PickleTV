# Welcome Screen Implementation

## Overview
PickleTV now starts with a welcome screen that provides two options:
1. **Play Demo Video** - Launches the video player immediately with the demo video
2. **Sign In with Email** - Shows an email entry form for user authentication

## Implementation Details

### Files Created/Modified

1. **WelcomeActivity.kt** (NEW)
   - Main welcome screen with two buttons
   - Email sign-in form with validation
   - Navigation between welcome and sign-in screens
   - Passes MODE and EMAIL to MainActivity via Intent extras

2. **AndroidManifest.xml** (MODIFIED)
   - WelcomeActivity is now the LAUNCHER activity
   - MainActivity is now a secondary activity (not exported)

3. **MainActivity.kt** (MODIFIED)
   - Now receives MODE and EMAIL from WelcomeActivity
   - Logs the mode for debugging purposes
   - MODE can be "DEMO" or "SIGNED_IN"

## User Flow

### Demo Video Flow
1. User launches PickleTV
2. Welcome screen appears with "Play Demo Video" and "Sign In with Email" buttons
3. User selects "Play Demo Video"
4. MainActivity launches with MODE="DEMO"
5. Video starts playing immediately

### Sign-In Flow
1. User launches PickleTV
2. Welcome screen appears
3. User selects "Sign In with Email"
4. Email entry form appears with:
   - Email input field
   - "Sign In" button
   - "Back" button to return to welcome screen
5. User enters email address
6. Email validation checks:
   - Non-empty email
   - Valid email format
7. If valid, MainActivity launches with MODE="SIGNED_IN" and EMAIL="user@example.com"
8. If invalid, error toast is shown

## Navigation Controls

- **Back/Escape Key**: When on sign-in screen, returns to welcome screen
- **DPAD/Arrow Keys**: Navigate between buttons
- **Enter/Select**: Activate selected button

## Future Enhancements

The sign-in functionality currently:
- Validates email format
- Passes email to MainActivity
- Shows a toast confirmation

To add real authentication:
1. Add API endpoint for sign-in
2. Store authentication token
3. Add loading indicator during sign-in
4. Handle authentication errors
5. Add sign-out functionality
6. Remember signed-in user

## Testing

The implementation has been:
- ✅ Successfully compiled
- ✅ Successfully installed on device
- ✅ Verified in logcat that WelcomeActivity launches
- ✅ Verified that "Play Demo Video" launches MainActivity with MODE="DEMO"
- ✅ Verified video playback works after launching from welcome screen

## Code Structure

### WelcomeActivity UI Components
- **titleText**: Welcome message
- **demoButton**: Play Demo Video button (default focus)
- **signInButton**: Sign In with Email button
- **signInLayout**: Container for sign-in form (hidden by default)
  - emailInput: Email entry field
  - submitButton: Sign In button
  - backButton: Return to welcome screen

### State Management
- `showingSignIn`: Boolean flag tracking which screen is visible
- Email validation using Android's Patterns.EMAIL_ADDRESS

### Intent Extras
- `MODE`: String - "DEMO" or "SIGNED_IN"
- `EMAIL`: String - User's email address (only for SIGNED_IN mode)


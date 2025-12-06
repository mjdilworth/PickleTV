# Magic Link Authentication

## Overview
The Sign In tab now uses **magic link authentication** for secure, passwordless sign-in to the dil.map Android TV app.

## How It Works

### User Flow
1. Navigate to the **Sign In** tab
2. Enter your email address
3. Press **"Send Magic Link"**
4. Check your email for the magic link
5. Click the link to complete sign-in on your TV device

### Technical Implementation

#### Device Identification
Each Android TV device is uniquely identified using:
- **Device ID**: `Settings.Secure.ANDROID_ID` 
- **Device Model**: `android.os.Build.MODEL`
- **Device Manufacturer**: `android.os.Build.MANUFACTURER`

#### API Endpoint
**Request Magic Link:**
```
POST https://tv.dilly.cloud/api/auth/magic-link
Content-Type: application/json

{
  "email": "user@example.com",
  "deviceId": "abc123...",
  "deviceModel": "SHIELD Android TV",
  "deviceManufacturer": "NVIDIA",
  "platform": "android-tv"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Magic link sent to user@example.com"
}
```

**Verify Magic Link:**
```
GET https://tv.dilly.cloud/api/auth/verify?token=TOKEN&deviceId=DEVICE_ID
```

**Response:**
```json
{
  "email": "user@example.com",
  "userId": "user_123",
  "deviceId": "abc123..."
}
```

### Security Features

1. **Device Binding**: Magic links are tied to the specific device that requested them
2. **Email Verification**: User must have access to the email account
3. **Unique Token**: Each magic link contains a unique, time-limited token
4. **No Password Storage**: No passwords are stored or transmitted

### Files Created

- **`app/src/main/java/com/dilworth/dilmap/auth/MagicLinkService.kt`**
  - Handles magic link requests and verification
  - Manages device ID retrieval
  - Network communication with auth API

### UI Components

The Sign In screen (`SignInScreen` composable) includes:
- Email input field
- Device ID display (read-only)
- "Send Magic Link" button with loading state
- Status messages (success/error)
- Cancel button to return to Browse Content

### Backend Requirements

The backend server at `https://tv.dilly.cloud/api` must implement:

1. **`POST /auth/magic-link`**
   - Generate unique token
   - Store token with device ID and expiration
   - Send email with magic link URL
   - Return success/error response

2. **`GET /auth/verify`**
   - Validate token and device ID match
   - Check token hasn't expired
   - Return user information
   - Invalidate token after use

### Example Email Template

```
Subject: Sign in to dil.map on your TV

Hi there!

Click the link below to sign in to dil.map on your Android TV:

https://tv.dilly.cloud/auth/verify?token=UNIQUE_TOKEN

This link will expire in 15 minutes and can only be used once.

Device: SHIELD Android TV (NVIDIA)
Device ID: abc123...

If you didn't request this, you can safely ignore this email.

---
dil.map by Dilworth Creative LLC
```

### Future Enhancements

- [ ] QR code for easier mobile-to-TV authentication
- [ ] Remember device for automatic re-authentication
- [ ] Session management and token refresh
- [ ] Multi-device support for single account
- [ ] Deep linking to handle magic link clicks directly in the app

### Testing

To test the magic link feature:

1. Navigate to Sign In tab
2. Enter test email: `test@lucindadilworth.com`
3. Check device ID is displayed
4. Click "Send Magic Link"
5. Verify status message appears
6. Check server logs for API request

### Error Handling

The app handles various error scenarios:
- **Invalid email format**: Shows validation error
- **Network errors**: Shows network error message
- **Server errors**: Displays server error message
- **Empty email**: Disables send button

All errors are displayed in red status messages that auto-dismiss after 5 seconds.

### Logging

All magic link operations are logged with tag `MagicLinkService` for debugging:
- Magic link requests
- Successful sends
- Verification attempts
- Error conditions

Check logs with:
```bash
adb logcat -s MagicLinkService SignInScreen
```


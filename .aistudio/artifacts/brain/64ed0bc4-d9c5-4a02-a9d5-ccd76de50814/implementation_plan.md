# Implementation Plan - Admin Banner Image Upload & URL Support

## User Goal
Allow the admin to add banner images either by pasting an Image URL or picking an image from the device gallery (using Android Photo Picker), and display these banner images in the home screen carousel using Coil image loading.

## Proposed Changes

### 1. Dependencies & Permissions
- Verify `coil-compose` is enabled in `app/build.gradle.kts` for loading banner images from URLs or URIs.

### 2. Database Entity Update
- Ensure `BannerEntity` has `imageUrl: String` (can store either a remote URL string or a local file content URI string).

### 3. Admin Banners Panel (`AdminMainScreen.kt`)
- Add an Image URL text input field.
- Add a "Pick Image from Gallery" button using `ActivityResultContracts.PickVisualMedia()` to allow admins to select image files from device storage.
- Save the resulting URL/URI in `BannerEntity`.

### 4. User Home Screen Carousel (`UserMainScreen.kt`)
- Update `BannerCard` to load banner images using Coil (`AsyncImage`) with a fallback gaming gradient background if no image is provided.

## Verification Plan
- Compile applet with `compile_applet`.
- Verify admin banner creation with image URL / local URI and correct display in carousel.

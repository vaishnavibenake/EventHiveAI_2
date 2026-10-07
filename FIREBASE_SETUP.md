# Firebase setup

This Android app uses Firebase Authentication and Cloud Firestore. `app/google-services.json` must belong to the Firebase project that will host the app.

## Deploy Firestore rules

From the repository root, install/use the Firebase CLI and run:

```sh
firebase deploy --only firestore:rules
```

The rules in `firestore.rules` make event proposals private to admins and their coordinator until approval. Students can read and register only for approved events. Coordinator account roles and club assignments must be granted by an administrator in the Firebase Console or trusted server code.

## First administrator

1. Enable Email/Password in Firebase Authentication.
2. Create the first administrator account in Authentication.
3. In Firestore, create `users/{AUTH_UID}` for that account with `role: "ADMIN"`, plus `name` and `email`.
4. Create coordinator accounts in Authentication and `users/{AUTH_UID}` profiles with `role: "COORDINATOR"` and a valid `clubId`.
5. Create the referenced club documents in `clubs/{clubId}`.
6. Students can create accounts through the app; their profile role is always `STUDENT`.

Never grant roles based on the selected login screen. The app reads the role from the authenticated user's Firestore profile, and the rules prevent users from changing their own role.

Demo data is not seeded automatically. Add real clubs/events through the administrator workflows, or seed development data from a trusted script using the Firebase Admin SDK.

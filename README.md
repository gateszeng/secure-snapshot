UnifyID Coding Challenge

How To Build:
Open the project in Android Studio
Then press the green triangle to build and run

Further Considerations:
I had some issues setting up the Camera object in the beginning. While trying to
take the 10 photos, the application would crash if the Timer was repeating at a
a value below 2 seconds.  Therefore, I utilized a different object which was the
CountDownTimer.  Using this timer, I was able to take the 10 images and 0.5
second intervals.

The biggest challenge for me was getting the images to encrypt.
I decided to store the images on the Internal Storage and set it to private so
that the data would only be private to this application. This would provide
sufficient security as long as a device isn't rooted.  However, in the case that
an attacker did gain root access, I encrypted the data as well. Not having much
experience encrypting files, I looked through the documentation for Android's
Keystore, believing that it would help me generate a key to encrypt my data.
However, I had a lot of trouble in generating this key, since the SecretKey that
is received from the Keystore cannot be decoded into binary to use in a cipher.
Therefore, I settled on using DES with a user supplied password to encrypt the
10 photos.

To improve this application in the future, I could add a counter or visual
indicator so the user knows when the image is being taken.  Furthermore, I would
change the encryption algorithm to something more secure secure than DES.

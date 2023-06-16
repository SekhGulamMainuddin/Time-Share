# Time-Share

Social Media App built using Kotlin and XML. It has functionalities like Chatting(1-1 and Group), Content Sharing(Image, GIF, Video, PDF, etc.), Voice and Video Conferencing(One to One and Group), Reels, Stories, Posts, etc. BackEnd is built on Firebase (Firestore, Realtime DB, Firebase Storage, and FCM). Authentication and Sign-Up using Email-Password|Google Sign-In|Phone Number-OTP. Used Agora.io for Voice and Video Conferencing.
  <p><br></p>
  <h3 align="left">Languages and Tools Used:</h3>
<p> 
  <a href="https://developer.android.com" target="_blank" rel="noreferrer"> <img src="https://user-images.githubusercontent.com/73953395/221414435-71f899f1-d053-4ab6-b235-12e253b2bbd8.png" alt="android" width="75" height="75"/> </a>
  <a href="https://kotlinlang.org" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-icon.svg" alt="kotlin" width="75" height="75"/> </a> 
  <a href="https://www.w3schools.com/xml/" target="_blank" rel="noreferrer"> <img src="https://cdn-icons-png.flaticon.com/128/136/136526.png" alt="XML" width="75" height="75"/> </a>
  <a href="https://firebase.google.com/" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/firebase/firebase-icon.svg" alt="firebase" width="75" height="75"/> </a> 
  <a href="https://www.agora.io/en/" target="_blank" rel="noreferrer"> <img src="https://www.agora.io/en/wp-content/themes/agora-main/images/agora-logo.svg" alt="agora" width="100" height="60"/> </a> &nbsp; 
</p>
  
  <p><br></p>

## TechStack Used <img src="https://cdn-icons-png.flaticon.com/128/4997/4997543.png" width="25" height="25">

**Client:**  Kotlin, XML, CMake, C++ 

**Server & Backend:**  Firebase, Firebase Firestore, Firebase Real-Time Database, Firebase Storage, Firebase Auth, FCM

## Features
- Posts
- Status & Stories
- Reels
- Follow and Friend
- Message (1-1 and Group)
- File & Content Sharing (Image, GIF, Video, PDF, etc.)
- Voice & Video Calling (1-1 and Group)
- Share Reel & Share Post
- Voice Message
- Activity Status
- Notification of Events and Messages

## Installation
 - First of all Clone the repository
```
git clone https://github.com/SekhGulamMainuddin/Time-Share.git
```
- Create a [Firebase](https://console.firebase.google.com/) Project
- Open the Cloned Repo in Android Studio and Connect it to the Firebase Project 
- Generate your SHA-1 or SHA-2 key and [ADD](https://stackoverflow.com/questions/39144629/how-to-add-sha-1-to-android-application) it to your Firebase Project
```
keytool -list -v -keystore "C:\Users\ReplaceThisWithYourUserName\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
- After connected to firebase download and place the google-services.json at [app/src/](https://i.stack.imgur.com/BFmz5.png)
- Create an [Agora](https://console.agora.io/) Project 
- Retrieve the [App-Id]() and [App-Certificate]() from Agora Project
- Also Create a [Giphy](https://developers.giphy.com/) Project
- Retrieve the API key for Giphy
- Store all the above keys using CMake -> [Prefer this article](https://www.codementor.io/blog/kotlin-apikeys-7o0g54qk5b)
- Remember to follow this structure in Kotlin Part 
```
object Keys {
    init {
        System.loadLibrary("native-lib")
    }
    external fun apiKey(): String
    external fun giphyKey(): String
    external fun getAppIdAgora(): String
    external fun getAppCertificateAgora(): String
    external fun getFCMKey(): String
    external fun getProjectNumber(): String
}
```
and this structure in CPP part
```
#include <jni.h>
#include <string>
extern "C" JNIEXPORT jstring
JNICALL
Java_com_sekhgmainuddin_timeshare_utils_Keys_apiKey(JNIEnv *env, jobject object) {
    std::string api_key = "<your_api_key_goes_here>";
    return env->NewStringUTF(api_key.c_str());
}

JNICALL
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sekhgmainuddin_timeshare_utils_Keys_giphyKey(JNIEnv *env, jobject thiz) {
    std::string giphy_api_key = "<your-giphy-api-key>";
    return env->NewStringUTF(giphy_api_key.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sekhgmainuddin_timeshare_utils_Keys_getAppIdAgora(JNIEnv *env, jobject thiz) {
    std::string app_id = "<your-agora-app-id>";
    return env->NewStringUTF(app_id.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sekhgmainuddin_timeshare_utils_Keys_getAppCertificateAgora(JNIEnv *env, jobject thiz) {
    std::string app_certificate = "<your-agora-app-certificate>";
    return env->NewStringUTF(app_certificate.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sekhgmainuddin_timeshare_utils_Keys_getFCMKey(JNIEnv *env, jobject thiz) {
    std::string server_key = "<your-fcm-server-key>";
    return env->NewStringUTF(server_key.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sekhgmainuddin_timeshare_utils_Keys_getProjectNumber(JNIEnv *env, jobject thiz) {
    std::string project_number = "<your-firebase-project-number-required-for-group-notification>";
    return env->NewStringUTF(project_number.c_str());
}
```
- Now Build the project and run it on your Device or Emulator
- Some indexing may be required on the Firebase Firestore end for multiple queries.

## Libraries Used
- Firebase (Auth, Storage, Firestore, Realtime-DB) - Authentication and Data Storage
- FCM - For Notification
- Agora - Voice and Video Calling
- Dagger-Hilt - Dependency Injection
- Retrofit2 - For calling FCM API for Notification
- RoomDB - For offline data caching
- Glide - For Image Processing
- Giphy - For showing GIF
- Exoplayer - For playing remote videos smoothly
- Kotlin Serialization - For JSON-Parsing and Object Parsing
- Other Libraries are Kotlin Coroutines, Navigation Component, ViewModels, CameraX, LottieAnimations, etc.













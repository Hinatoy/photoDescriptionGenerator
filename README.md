# PhotoDescriptionGenerator
A Java application that uses AWS Rekognition and Polly to create a short description of a given photo.

For determining if there are any people in the photo and details about them (including their genders, ages, moods etc.), Rekognition's Facial Analysis is used (function: _describeFaces()_). 
For determining people's clothes and their surroundings, Rekognition's Label Detection is used (functions: _describeScenery()_ and _describeClothes()_).

These three functions generate the text that gets passed to function _playFinalDescription()_, which uses Polly to generate human-like speech. 

An extremely simple Spring GUI is used for interface that lets a user search for a photo on their computer via a File Chooser.

<img>1.jpg</img>

## Results
The program does seem to produce somewhat accurate descriptions most of the time. There are certain types of photos that get extremely accurate descriptions (photos of nature, selfies, etc.) and fairly inaccure (photos with lots of people).

sed -i "s/remapMixins = false/remapMixins = true/g" G:/Programs/IdeaProjects/Cold-Sweat/src/main/java/dev/momostudios/coldsweat/ColdSweat.java
./gradlew build
sed -i "s/remapMixins = true/remapMixins = false/g" G:/Programs/IdeaProjects/Cold-Sweat/src/main/java/dev/momostudios/coldsweat/ColdSweat.java

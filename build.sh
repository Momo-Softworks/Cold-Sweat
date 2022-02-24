sed -i "s/remapMixins = false/remapMixins = true/g" C:/Users/gamer/IdeaProjects/Cold-Sweat-Rework-Forge/src/main/java/dev/momostudios/coldsweat/ColdSweat.java
./gradlew build
sed -i "s/remapMixins = true/remapMixins = false/g" C:/Users/gamer/IdeaProjects/Cold-Sweat-Rework-Forge/src/main/java/dev/momostudios/coldsweat/ColdSweat.java

sed -i "s/REMAP_MIXINS = false/REMAP_MIXINS = true/g" C:/Projects/IdeaProjects/Cold-Sweat/src/main/java/dev/momostudios/coldsweat/ColdSweat.java
./gradlew build
sed -i "s/REMAP_MIXINS = true/REMAP_MIXINS = false/g" C:/Projects/IdeaProjects/Cold-Sweat/src/main/java/dev/momostudios/coldsweat/ColdSweat.java

# Cold-Sweat
Cold Sweat is a mod for Minecraft 1.20, 1.19, 1.18, 1.16, and 1.7 that adds a comprehensive temperature system to the game. This is the public repository for our project on CurseForge:  
https://www.curseforge.com/minecraft/mc-mods/cold-sweat  
  
This project is completely open for reference and reproduction as per our GNU GPL 3.0 license; HOWEVER, any project that directly samples code, assets, or otherwise from Cold Sweat must also list their project under the GNU General Public License 3.0 and attribute this project (also defined in the license).

## Documentation
Documentation for 3rd-party integration with Cold Sweat can be found here (WIP, out-of-date for 2.2+):  
https://mikul.gitbook.io/cold-sweat/  
The current documentation is only designed for 1.18.2, but it should be useable for 1.16.5 and 1.19.2 as this mod aims for as much backend parity as possible.

## Developing for CS
1. Get [Cursemaven](https://www.cursemaven.com/)
2. Go to the latest version on [CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/cold-sweat/files) and add the ColdSweat-sources.jar file as a dependency

(i.e. `implementation "curse.maven:cold-sweat-506194:5011469-sources-5011470"` gives you version 2.2.5 for 1.20 with the `sources` jar for your development environment)

targetdir=target

if [ ! -d "$targetdir" ]; then mkdir $targetdir; fi

javac -sourcepath src -d $targetdir -extdirs lib/ src/core/*.java src/movement/*.java src/report/*.java src/routing/*.java src/routing/dstn/*.java src/dtsn/*.java src/dtsn/tools/*.java src/dtsn/Exception/*.java src/sdtp/*.java src/sdtp/tool/*.java src/stwsn/*.java src/stwsn/tools/*.java src/gui/*.java src/input/*.java src/applications/*.java src/interfaces/*.java

if [ ! -d "$targetdir/gui/buttonGraphics" ]; then cp -R src/gui/buttonGraphics target/gui/; fi
	

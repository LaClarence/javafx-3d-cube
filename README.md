# One Formula That Demystifies 3D Graphics

This is a JavaFX 27 port of the project from the YouTuber Tsoding's video
[One Formula That Demystifies 3D Graphics](https://www.youtube.com/watch?v=qjWkNZ0SXfo&t=7s)

The purpose is to render a simple 3D perspective projection (x/z, y/z) of a wireframe cube rotating on the Y axis.

References:
- [Rotation Matrix](https://www.youtube.com/watch?v=EZufiIwwqFA)
- [Penger Model](https://github.com/Max-Kawula/penger-obj)
- [Source Code](https://github.com/tsoding/formula)

## Javascript to Java port

To replace the browser, the following framework has been used: 
- Java 27
- JavaFX 27
- Gradle 9.8.1 (updated with ./gradlew wrapper --gradle-version 9.8.1)

The Javascript code is provided at the end of the file.

## Run

Linux / macOS:

```bash
./gradlew run
```

Windows:

```powershell
gradlew.bat run
```

## Build

### Distribution (requires Java installed on the target machine)

Produces a launch script and all JARs under `build/install/`:

```bash
./gradlew installDist
./build/install/javafx-3d-cube/bin/javafx-3d-cube
```

### Self-contained native application (no Java required)

Produces a macOS `.app` bundle under `build/jpackage/` with an embedded JRE:

```bash
./gradlew jpackage
open build/jpackage/CubeDemo.app
```

To generate a `.dmg` installer (drag-and-drop), change `build.gradle`:

```
'--type', 'app-image',
by
'--type', 'dmg',
```

## The JS code presented

Here the html code
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>One Formula That Demystifies 3D Graphics</title>
</head>
<body>
<canvas id="game" width="800" height="800"></canvas>
<script src="index.js" defer></script>
</body>
</html>
```

and the index.js file:
```Javascript
console.log('3D cube ✨');

const BACKGROUND_COLOR = '#101010';
const FOREGROUND_COLOR ='#50FF50';
const LINE_WIDTH = 3;
const game = document.querySelector('#game');
const ctx = game.getContext('2d');

function clear() {
    ctx.fillStyle = BACKGROUND_COLOR;
    ctx.fillRect(0, 0, game.width, game.height);
}

function point({x,y}) {
    const s = 20;
    ctx.fillStyle = FOREGROUND_COLOR;
    ctx.fillRect(x - s/2, y -s/2, s, s);
}

function line(p1, p2) {
    ctx.lineWidth = LINE_WIDTH;
    ctx.strokeStyle = FOREGROUND_COLOR;
    ctx.beginPath();
    ctx.moveTo(p1.x, p1.y);
    ctx.lineTo(p2.x, p2.y);
    ctx.stroke();
}

function screen(p) {
    // -1..1 => 0..2 => 0..1 => 0..w
    return {
        x: ((p.x + 1) / 2) * game.width,
        y: (1 - ((p.y + 1) / 2) ) * game.height
    };
}

function project({x, y, z}) {
    return {
        x: x / z,
        y: y / z,
    };
}

function translate_z({x, y, z}, dz) {
    return {x, y, z: z +dz}
}

function rotate_xy({x, y, z}, angle) {
    const cosAngle = Math.cos(angle);
    const sinAngle = Math.sin(angle);
    return {
        x: x * cosAngle - z * sinAngle,
        y,
        z: x * sinAngle + z * cosAngle,
    }
}

const vs = [
    // Back face of the cube
    {x:  0.25, y:  0.25, z:  0.25},
    {x: -0.25, y:  0.25, z:  0.25},
    {x: -0.25, y: -0.25, z:  0.25},
    {x:  0.25, y: -0.25, z:  0.25},
    // Front face of the cube
    {x:  0.25, y:  0.25, z: -0.25},
    {x: -0.25, y:  0.25, z: -0.25},
    {x: -0.25, y: -0.25, z: -0.25},
    {x:  0.25, y: -0.25, z: -0.25},
];

const fs = [
    [0, 1, 2, 3],
    [4, 5, 6, 7],
    [0, 4],
    [1, 5],
    [2, 6],
    [3, 7],
];

const FPS = 60;
const ROT_SPEED = 1; // 2 => faster
let dz = 1;
let angle = 0;

function frame() {
    const dt = 1 /FPS;
    //dz += 1*dt;
    angle += ROT_SPEED*Math.PI*dt;
    clear();
    //for(const v of vs) {
    //    point(screen(project(translate_z(rotate_xy(v,angle), dz))));
    //}
    for (const f of fs) {
        for (let i = 0; i < f.length; ++i) {
            const a = vs[f[i]];
            const b = vs[f[(i+1)%f.length]];
            line(
                screen(project(translate_z(rotate_xy(a,angle), dz))),
                screen(project(translate_z(rotate_xy(b,angle), dz)))
            )

        }
    }
    setTimeout(frame, 1000/FPS);
}

setTimeout(frame, 1000/FPS);
```
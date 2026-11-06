# Pot Slot Lock

Mod para Fabric 1.20.1 que permite bloquear los slots de ingredientes de la olla de Farmer's Delight.

Autor: [leaenojqdo](https://github.com/leaenojqdo).

## Que hace

- Fija cada slot al item que tenia al activar el bloqueo.
- Impide que las tolvas pongan items distintos en los slots fijados.
- Deja vacios y deshabilitados para las tolvas los slots que estaban vacios.
- Permite seguir modificando la olla manualmente.
- Permite desbloquear los slots para volver al comportamiento normal.

## Cambios de la version 1.0.2

- El boton para bloquear o desbloquear los slots ahora aparece centrado debajo del inventario del jugador.
- Se agrego compilacion automatica con GitHub Actions y publicacion del JAR como Release.
- Se actualizo el autor mostrado en los metadatos del mod a `leaenojqdo`.

## Descargar e instalar

Descarga `potlock-1.0.2.jar` desde [Releases](https://github.com/leaenojqdo/Potlock-mod/releases/latest) y colocalo en la carpeta `mods` del perfil de Minecraft. Necesitas Fabric y Farmer's Delight para Minecraft 1.20.1.

## Compilar

Requiere Java 17. Desde la carpeta del proyecto:

```sh
cd potlock-source
./gradlew build
```

El JAR se genera en `potlock-source/build/libs/`.

## GitHub Actions

Cada push a `main` compila el mod y deja el JAR descargable como artifact en la ejecucion de Actions. Al publicar un tag con formato `v*` (por ejemplo, `v1.0.1`), Actions compila el proyecto y publica un Release publico con el JAR adjunto.


Hecho con IA

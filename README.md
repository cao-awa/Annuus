# Annuus

![](https://count.getloli.com/@@cao-awa.annuus?name=%40cao-awa.annuus&theme=rule34&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

Annuus is a mod for minecraft networking,
improves network performance and makes the game more available for running on large server.

Supported newest minecraft with fabric loader and neoforge loader, needs fabric-api when using fabric, no other mods requirements when using neoforge.

When the player doesn't install annuus on the client, network packet will send normally like vanilla.

# Performance

## Chunk data
> Deflate 1 has less CPU usage, even though that performance is not good enough here \
> So it be considered as a compression option

We have tested view distance as 10 (473), 12 (637), 16 (1057), 32 (3725), installed mod only fabric-api.

|         env/chunks |          473           |          637           |          1027          |           3725           |
|-------------------:|:----------------------:|:----------------------:|:----------------------:|:------------------------:|
|            Vanilla | 35.03ms <br /> 13.94MB | 40.51ms <br /> 18.74MB | 72.57ms <br /> 31.66MB | 238.69ms <br /> 122.40MB |
| Annuus (Deflate 9) | 29.76ms <br /> 1.17MB  | 50.46ms <br /> 2.39MB  |  48.6ms <br /> 4.04MB  | 163.31ms <br /> 15.98MB  |
| Annuus (Deflate 1) | 36.09ms <br /> 2.21MB  | 51.06ms <br /> 2.96MB  | 57.94ms <br /> 4.99MB  | 235.22ms <br /> 19.65MB  |

## Block updates
> The more blocks collect the more advantages in Annuus protocol, such as very huge piston towers    

And chunk delta update blocks, every time collect 1229 blocks:

|         env/blocks |  2455  |  7365   |  24550  |  78560   |
|-------------------:|:------:|:-------:|:-------:|:--------:|
|            Vanilla | 9.72KB | 29.16KB | 97.2KB  | 311.06KB |
| Annuus (Deflate 9) | 6.79KB | 20.39KB | 67.97KB | 217.53KB |
| Annuus (Deflate 1) | 6.35KB | 19.07KB | 63.59KB | 203.5KB  |

## Recipes synchronize

|                         env/recipes |   1290   |   9587   |
|-----------------------------------:|:--------:|:--------:|
|                            Vanilla | 105.73KB | 692.2KB  |
| Replacement v1 <br/> (no_compress) |  94.4KB  | 612.86KB |
|   Replacement v1 <br/> (deflate_9) | 19.61KB  | 135.9KB  |
|   Replacement v1 <br/> (deflate_1) | 24.11KB  | 169.11KB |

# Configuration
Config ``chunk_compression`` and ``block_updates_compression`` have options: ``no_compress``, ``lz4``, ``best_speed``, ``deflate_1``, ``deflate_2``, ``deflate_3``, ``deflate_4``, ``deflate_5``, ``deflate_6``, ``deflate_7``, ``deflate_8``, ``deflate_9``, ``best_compress``. 

The ``best_speed`` is alias of ``deflate_1``, ``best_compress`` is ``deflate_9``.

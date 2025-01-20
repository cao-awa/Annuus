# Annuus
![](https://count.getloli.com/@@cao-awa.annuus?name=%40cao-awa.annuus&theme=rule34&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

Annuus is a mod for minecraft networking,
improves network performance and makes the game more available for running on large server.

Supported minecraft 1.21.4 with fabric loader, needs fabric-api.

When the player doesn't install annuus on the client, network packet will send normally like vanilla.

# Performance

> Deflate 1 has less CPU usage, even though that performance is not good enough here \
> So it be considered as a compression option

We have tested view distance as 10 (473), 12 (637), 16 (1057), 32 (3725), installed mod only fabric-api.


|         env/chunks |          473           |          637           |          1027          |           3725           |
|-------------------:|:----------------------:|:----------------------:|:----------------------:|:------------------------:|
|            Vanilla | 35.03ms <br /> 13.94MB | 40.51ms <br /> 18.74MB | 72.57ms <br /> 31.66MB | 238.69ms <br /> 122.40MB |
| Annuus (Deflate 9) | 29.76ms <br /> 1.17MB  | 50.46ms <br /> 2.39MB  |  48.6ms <br /> 4.04MB  | 163.31ms <br /> 15.98MB  |
| Annuus (Deflate 1) | 36.09ms <br /> 2.21MB  | 51.06ms <br /> 2.96MB  | 57.94ms <br /> 4.99MB  | 235.22ms <br /> 19.65MB  |

# Compatibility
Not compatible to Sodium now, doesn't test with other mods.
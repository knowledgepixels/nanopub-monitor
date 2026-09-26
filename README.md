[![Coverage Status](https://coveralls.io/repos/github/knowledgepixels/nanopub-monitor/badge.svg?branch=master)](https://coveralls.io/github/knowledgepixels/nanopub-monitor?branch=master)
[![semantic-release: angular](https://img.shields.io/badge/semantic--release-angular-e10079?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# Nanopub Monitor

A webapp to monitor the nanopublication server network.

## Available Instances

You can find instances of the Nanopub Monitor here:

- https://monitor.knowledgepixels.com/
- https://monitor.petapico.org/

## What It Serves

- `/` - the status page, with one row per monitored service
- `/.json` - the same data as JSON, for machine consumption (this is what the
  [nanopub-router](https://github.com/knowledgepixels/nanopub-router) reads)
- `/.csv` - the same data as CSV

Every response carries a `Nanopub-Monitor-Version` header naming the running version.

## Running an Instance

The monitor is a single container and keeps no data of its own: it asks a [Nanopub
Query](https://github.com/knowledgepixels/nanopub-query) instance which services the network announces, tests each of
them every few seconds, and holds the results in memory. That makes it cheap to run and gives it nothing to back up,
but also means a restart starts the statistics from scratch.

It needs no configuration to run:

```bash
docker compose pull
docker compose up -d
```

`docker compose pull` fetches the released image from Docker Hub; without it, Compose builds the image from this
checkout instead, which works too but takes longer. To change any of the [settings](#settings), copy the template
first and edit your copy - which is ignored by git, so your instance's settings stay out of the way of `git pull`:

```bash
cp docker-compose.override.yml.template docker-compose.override.yml
```

Check that it came up:

```bash
curl -sI http://localhost:7890/ | grep -i '^nanopub-monitor'
```

The status page then takes a scan or two to fill in: services start out as `NOT SEEN` and get their first result
within a few seconds.

### Publishing It

The monitor serves plain HTTP on port `7890` of the host. To make the instance publicly reachable via HTTPS, run a
reverse proxy (e.g. nginx) on the host that terminates TLS and forwards to `localhost:7890`. The pages are public and
read-only, and the app has no authentication and no write endpoints, so nothing beyond TLS termination is needed. If
that proxy runs on the same host, you can bind the port to localhost as well, which Section 4 of the override template
shows how to do.

### Settings

Everything is optional, and all of it is set as environment variables in `docker-compose.override.yml`; see
[the template](docker-compose.override.yml.template) for the full list with its defaults. The ones worth knowing
about:

- `NANOPUB_QUERY_INSTANCES` - a whitespace-separated list of Nanopub Query instances (with trailing slash) to get the
  server list from. Left unset, the monitor finds them through the nanopub setting, which is what a public instance
  wants. Set it to point the monitor at a Query instance you run yourself. Without a reachable query instance the
  monitor comes up but has nothing to show.
- `NANOPUB_MONITOR_SCAN_FREQ` - seconds between two scans of the whole list (default `10`). Every scan tests every
  known server, so raising this lowers the load on the monitored network as well as on this host.
- `NANOPUB_MONITOR_GET_GEOIP_INFO` - whether to look up where the monitored servers are located (default `true`). The
  lookups go to the third-party service [ip-api.com](https://ip-api.com/), over plain HTTP, and send it the host names
  of the monitored servers. Set it to `false` on a host with restricted outbound access, or to keep those addresses to
  yourself, and switch `NANOPUB_MONITOR_SHOW_MAP` off along with it.

The defaults of all settings live in
[conf.properties](src/main/resources/ch/tkuhn/nanopub/monitor/conf.properties), and each can also be overridden by a
`local.conf.properties` on the classpath. That file is inside the packaged webapp, though, so for a Docker deployment
the environment variables are the way in.

### Upgrading

Releases are listed on [GitHub](https://github.com/knowledgepixels/nanopub-monitor/releases), with their changes in
the [changelog](CHANGELOG.md), and published as `nanopub/monitor` images on Docker Hub, tagged with their version. By
default the instance runs the `latest` tag. To control when upgrades happen, pin a version in `.env`:

```bash
NANOPUB_MONITOR_IMAGE_TAG=1.6.0
```

To upgrade, update the checkout as well, since `docker-compose.yml` changes along with the app, then restart:

```bash
git pull
docker compose pull
docker compose up -d
```

There is no state to migrate, so an upgrade is just a restart, and downgrading works the same way.

### Checking on an Instance

- **Is it up**: `curl -sI http://localhost:7890/` - the `Nanopub-Monitor-Version` header names the running version.
- **Logs**: `docker compose logs -f monitor`. The startup lines list every setting an environment variable changed,
  each scan cycle logs a line, and a rejected setting fails the deployment with a message naming the environment
  variable to fix. Add `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug` to `JAVA_OPTS` for more (see Section 2 of the
  template).
- **Machine-readable status**: `curl -s http://localhost:7890/.json` for what the page shows.

## Development

To test and develop the Nanopub Monitor locally, follow these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/knowledgepixels/nanopub-monitor.git
   cd nanopub-monitor
    ```
2. Run the application using Jetty server:
   ```bash
   ./mvnw clean jetty:run
   ```
   Add `-Djetty.port=8099` to serve on another port.
3. Open your web browser and navigate to `http://localhost:8080/` to access the Nanopub Monitor.

To build and run the Docker image from the local sources instead, run `./run.sh`.

## License

This software is made available under the MIT license. See LICENSE.txt for the details.

[![Coverage Status](https://coveralls.io/repos/github/knowledgepixels/nanopub-monitor/badge.svg?branch=master)](https://coveralls.io/github/knowledgepixels/nanopub-monitor?branch=master)
[![semantic-release: angular](https://img.shields.io/badge/semantic--release-angular-e10079?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# Nanopub Monitor

A webapp to monitor the nanopublication server network.

## Available Instances

You can find instances of the Nanopub Monitor here:

- https://monitor.knowledgepixels.com/
- https://monitor.petapico.org/

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
3. Open your web browser and navigate to `http://localhost:8080/` to access the Nanopub Monitor.
## [1.8.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.7.0...nanopub-monitor-1.8.0) (2026-10-26)

### Features

* discover test instances of Query services, not only of registries ([e82e64b](https://github.com/knowledgepixels/nanopub-monitor/commit/e82e64b08c98f60ee1ff7eaa3aba5910d02644e0))

### Bug Fixes

* report a grlc or monitor table without a data row as broken ([8bef88a](https://github.com/knowledgepixels/nanopub-monitor/commit/8bef88a9ebb9efb9b5c9328773d5e5e9607cc860)), closes [#87](https://github.com/knowledgepixels/nanopub-monitor/issues/87) [#88](https://github.com/knowledgepixels/nanopub-monitor/issues/88)

### Tests

* **deps:** update dependency jakarta.servlet:jakarta.servlet-api to v6.1.0 ([b370c54](https://github.com/knowledgepixels/nanopub-monitor/commit/b370c54a7a7c42d775b2f2c4eb8fe536201f6a07))

### Build and continuous integration

* **deps:** update dependency org.jacoco:jacoco-maven-plugin to v0.8.16 ([3b5d0ce](https://github.com/knowledgepixels/nanopub-monitor/commit/3b5d0ce741c671303c11d9355ad6138e981c2f8e))

### General maintenance

* setting next snapshot version [skip ci] ([a636866](https://github.com/knowledgepixels/nanopub-monitor/commit/a6368669e3c9b0a88643e633bb4cfe5b86765261))

## [1.7.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.6.0...nanopub-monitor-1.7.0) (2026-10-26)

### Features

* add a light/dark mode switch ([dcdb6df](https://github.com/knowledgepixels/nanopub-monitor/commit/dcdb6df77b178a253ab55c9a1afa6dcb4d6b57d1))
* **config:** read settings from environment variables ([5fb8a42](https://github.com/knowledgepixels/nanopub-monitor/commit/5fb8a425ed05bff3d7ae2614f90176f9edb9fbf1))
* make the server map interactive ([7678d70](https://github.com/knowledgepixels/nanopub-monitor/commit/7678d7031c9f2cfb2061985453110ad85c88a486))
* **monitor:** discover the network's registry test instances ([4e2c00c](https://github.com/knowledgepixels/nanopub-monitor/commit/4e2c00c04dffc9ee07d4d1c8adca8933bbcc8d7e)), closes [#62](https://github.com/knowledgepixels/nanopub-monitor/issues/62)
* **monitor:** show which region of the world each server is in ([a5d1165](https://github.com/knowledgepixels/nanopub-monitor/commit/a5d1165b1ad61ee2b57bbe851b352f1f29f10f53)), closes [#5](https://github.com/knowledgepixels/nanopub-monitor/issues/5)
* name the exact service type in each section, and open links in a new tab ([5db7e61](https://github.com/knowledgepixels/nanopub-monitor/commit/5db7e61964c62b6c65dbd1e459cf790bbb4f8ef0))
* scan the nanopub-router service type ([fffdcac](https://github.com/knowledgepixels/nanopub-monitor/commit/fffdcac2426f29e7f1f2b1bf23c1b4f81e2c1e53))
* show a compact type bar while scrolling down the sections ([ad8ab63](https://github.com/knowledgepixels/nanopub-monitor/commit/ad8ab63a855e2c97fefe76b315a8c6344cd1249d))
* split the server table into one section per service type ([183d80e](https://github.com/knowledgepixels/nanopub-monitor/commit/183d80e6ec67d1e80a1a0cc07c2af9155234ff0c))
* treat a self-declared query test instance as a test instance ([f6ca633](https://github.com/knowledgepixels/nanopub-monitor/commit/f6ca633f9760433191858e794e863dc5b6da709a)), closes [knowledgepixels/nanopub-query#200](https://github.com/knowledgepixels/nanopub-query/issues/200)

### Dependency updates

* **core-deps:** update dependency com.google.code.gson:gson to v2.15.0 ([2befd21](https://github.com/knowledgepixels/nanopub-monitor/commit/2befd213ee16be4d5b8a1b6816958cd6013587b0))
* **core-deps:** update dependency org.junit:junit-bom to v6.1.3 ([970fc68](https://github.com/knowledgepixels/nanopub-monitor/commit/970fc68cca4d94e7cbe069d1d2d30473bf280fda))
* **core-deps:** update dependency org.nanopub:nanopub to v1.92.0 ([b239d3b](https://github.com/knowledgepixels/nanopub-monitor/commit/b239d3b3276abb0297774b334798e759d2c9df11))
* **core-deps:** update dependency org.nanopub:nanopub to v1.93.0 ([0738186](https://github.com/knowledgepixels/nanopub-monitor/commit/0738186c99750c393fea7c0ca7a07ba1f42a6f9d))
* **core-deps:** update dependency org.nanopub:nanopub to v1.94.0 ([d63e9fb](https://github.com/knowledgepixels/nanopub-monitor/commit/d63e9fbcc0276a68608c54149fab45d60f84e327))
* **core-deps:** update dependency org.nanopub:nanopub to v1.95.0 ([42bde66](https://github.com/knowledgepixels/nanopub-monitor/commit/42bde66bb90c8009d7dac166611680d48251e9bd))
* **core-deps:** update dependency org.slf4j:slf4j-simple to v2.0.19 ([0aac46f](https://github.com/knowledgepixels/nanopub-monitor/commit/0aac46f104f4dfdeed3498a2dd4256dfb597497b))
* **core-deps:** update dependency org.slf4j:slf4j-simple to v2.0.20 ([ea94bb2](https://github.com/knowledgepixels/nanopub-monitor/commit/ea94bb2f2d4c6af0ac1ebb649ca90bec314d1b42))
* **core-deps:** update wicket.version to v10.11.0 ([fdbed52](https://github.com/knowledgepixels/nanopub-monitor/commit/fdbed52cf765ca5311b7dc35bdaf1ad4b0a07aa4))
* **deps:** update dependency maven to v3.10.0 ([72c28d9](https://github.com/knowledgepixels/nanopub-monitor/commit/72c28d979b5d83c54b1b405fce35a39d5455d62e))
* **deps:** update tomcat docker tag to v11.0.25 ([8ea7008](https://github.com/knowledgepixels/nanopub-monitor/commit/8ea700819c3247bdbb1d4fce3d7d3cf6d91aa6a7))
* **deps:** update tomcat docker tag to v11.0.26 ([8fe730c](https://github.com/knowledgepixels/nanopub-monitor/commit/8fe730c30c1a17604186597b025bfedf72a8de8b))
* **deps:** update tomcat docker tag to v11.0.27 ([4637188](https://github.com/knowledgepixels/nanopub-monitor/commit/463718834bf37e643256979930609fcbb2d11798))

### Bug Fixes

* **docker:** make the application log visible and bounded ([fa9dee3](https://github.com/knowledgepixels/nanopub-monitor/commit/fa9dee3318b4af2742b779c923c1fd9fe6216c2a))
* don't fail the page when the monitor's own location has no coordinates ([8ca478d](https://github.com/knowledgepixels/nanopub-monitor/commit/8ca478da34e8fbd5ec6d45c7e51b701121414c65))
* keep the type bar shown after jumping to a section from it ([522d5e6](https://github.com/knowledgepixels/nanopub-monitor/commit/522d5e69ae588ddaf11b7ac72dd6339d3dfbfc3e))
* make NanopubService serializable ([d44cbdf](https://github.com/knowledgepixels/nanopub-monitor/commit/d44cbdf84a3b3c1ed788737e831c747270c203b5))
* make the server list safe to use from several threads ([89ec17c](https://github.com/knowledgepixels/nanopub-monitor/commit/89ec17cd66df5cc528ec62002f19c50cf8708d3c))
* **monitor:** report no consensus when a cohort splits evenly ([b13c5fc](https://github.com/knowledgepixels/nanopub-monitor/commit/b13c5fcd771b8d0e95c1a59f23adad7bc63320ca))

### Documentation

* describe how to run an instance on your own server ([4e97053](https://github.com/knowledgepixels/nanopub-monitor/commit/4e970533b860c6d56f7e69ccc5af044ad8ec4b36))

### Tests

* cover MonitorPage and MonitorApplication with WicketTester ([4ef6bd0](https://github.com/knowledgepixels/nanopub-monitor/commit/4ef6bd006df44d3a25244397c9c88d242664115a))
* cover the scan cycle, registry refresh and IP lookup ([a9fd424](https://github.com/knowledgepixels/nanopub-monitor/commit/a9fd42450e3cf89a506aacfe6eee976abe5603c0))
* raise unit test line coverage from 23.5% to 74.2% ([c3ed537](https://github.com/knowledgepixels/nanopub-monitor/commit/c3ed537d8869708d813ea4e308ba14fcb3f912c1))

### Build and continuous integration

* **deps:** lock file maintenance ([3b360e4](https://github.com/knowledgepixels/nanopub-monitor/commit/3b360e47f45fcbcc16dce4d0d3ad6a3cba170b6f))
* **deps:** update actions/setup-java action to v5.7.0 ([40d256b](https://github.com/knowledgepixels/nanopub-monitor/commit/40d256b494d6f00ff0e08dd537d504c1c4afcea9))
* **deps:** update actions/setup-java action to v6 ([e7b398e](https://github.com/knowledgepixels/nanopub-monitor/commit/e7b398ec7d85b128258c5e034b711524ddc65812))
* **deps:** update actions/setup-java action to v6.0.1 ([d87d258](https://github.com/knowledgepixels/nanopub-monitor/commit/d87d258333e26b2531a83faec2916a91f77314c1))
* **deps:** update actions/setup-java action to v6.1.0 ([cc3bd21](https://github.com/knowledgepixels/nanopub-monitor/commit/cc3bd21b2a80db1a23138b311ce0f635c0b11949))
* **deps:** update actions/setup-node action to v7.1.0 ([6c4048c](https://github.com/knowledgepixels/nanopub-monitor/commit/6c4048c87337770de46151c2c5deed7d27a86e04))
* **deps:** update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.16.0 ([7f27eee](https://github.com/knowledgepixels/nanopub-monitor/commit/7f27eeece624685482b275148e27b4c82146a0cd))
* **deps:** update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.6.0 ([da2f1e4](https://github.com/knowledgepixels/nanopub-monitor/commit/da2f1e4359df216d65d0ef629b6bb7c4f7e7ee6d))
* **deps:** update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.6.1 ([550a784](https://github.com/knowledgepixels/nanopub-monitor/commit/550a7840ebdb948a761b717a7cc31647bfa914f1))

### General maintenance

* **readme:** add new monitoring instance URL ([6a2a907](https://github.com/knowledgepixels/nanopub-monitor/commit/6a2a907e9b39bdedd1fdcb7fb28fa40afbfe70d8))
* setting next snapshot version [skip ci] ([d8dacde](https://github.com/knowledgepixels/nanopub-monitor/commit/d8dacdef5687b65538662ff6e56b4642146e58f1))

## [1.6.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.5.0...nanopub-monitor-1.6.0) (2026-07-31)

### Features

* **monitor:** flag query instances that fall behind their own registry ([143d105](https://github.com/knowledgepixels/nanopub-monitor/commit/143d10591866230449854700b9f8e4953bd2e739))

### Dependency updates

* **core-deps:** update dependency org.junit:junit-bom to v6.1.1 ([f021786](https://github.com/knowledgepixels/nanopub-monitor/commit/f02178634105c96e1c44e6b5c9e8d2a4b8f5e125))
* **core-deps:** update dependency org.junit:junit-bom to v6.1.2 ([058dd20](https://github.com/knowledgepixels/nanopub-monitor/commit/058dd208890b9979f0130a6cf2b72d5d04f67d03))
* **core-deps:** update dependency org.nanopub:nanopub to v1.91.0 ([8243d2a](https://github.com/knowledgepixels/nanopub-monitor/commit/8243d2a2c787c23997fc31183558d0063dedc056))
* **core-deps:** update wicket.version to v10.10.0 ([a18fd49](https://github.com/knowledgepixels/nanopub-monitor/commit/a18fd4994be2568078dc314eeb970a55c017f19c))
* **deps:** update tomcat docker tag to v11.0.23 ([ae3d787](https://github.com/knowledgepixels/nanopub-monitor/commit/ae3d7876d0305033983fbb483eda73dc7f4470c8))
* **deps:** update tomcat docker tag to v11.0.24 ([038d44a](https://github.com/knowledgepixels/nanopub-monitor/commit/038d44a6967facbd9f21707fa549acd2b46beb70))

### Bug Fixes

* initialize logger before eager singletons in static init ([b186232](https://github.com/knowledgepixels/nanopub-monitor/commit/b186232ad412b853f933d754f3f479fd0837278c))

### Documentation

* **ServerData:** update broken javadoc annotation ([e3e1792](https://github.com/knowledgepixels/nanopub-monitor/commit/e3e1792c776deef162aab7325b8c2e581ea7e11a))

### Build and continuous integration

* **deps:** lock file maintenance ([b52f4af](https://github.com/knowledgepixels/nanopub-monitor/commit/b52f4afbae28e1844a1002fc99f91570d7b62c9b))
* **deps:** lock file maintenance ([2f7890e](https://github.com/knowledgepixels/nanopub-monitor/commit/2f7890e69d53ceeeacfb25bce9bd943ae9ea3f7a))
* **deps:** lock file maintenance ([e1c3e20](https://github.com/knowledgepixels/nanopub-monitor/commit/e1c3e20c5f405676d014ea6553e937dd7f799835))
* **deps:** update actions/checkout action to v7.0.1 ([92bb090](https://github.com/knowledgepixels/nanopub-monitor/commit/92bb0904819092cf7f0af0ed930a17c4c10da5e3))
* **deps:** update actions/setup-java action to v5.4.0 ([13b3c53](https://github.com/knowledgepixels/nanopub-monitor/commit/13b3c53cc2dc0f2042447a6026cb7e6bfbd42495))
* **deps:** update actions/setup-java action to v5.5.0 ([d936240](https://github.com/knowledgepixels/nanopub-monitor/commit/d93624044ad373e85911548e29d3e419c363cf6e))
* **deps:** update actions/setup-java action to v5.6.0 ([91c8840](https://github.com/knowledgepixels/nanopub-monitor/commit/91c884097f311259288f5a586f6063e278d47a7a))
* **deps:** update actions/setup-node action to v7 ([6521e0e](https://github.com/knowledgepixels/nanopub-monitor/commit/6521e0e9b5e067dab2d39c34b2a6776660c03c38))
* **deps:** update coverallsapp/github-action action to v2.3.8 ([118844f](https://github.com/knowledgepixels/nanopub-monitor/commit/118844f4e7267524066d647fb14afc916b745edf))
* **deps:** update dependency com.google.cloud.tools:jib-maven-plugin to v3.5.2 ([5af8b08](https://github.com/knowledgepixels/nanopub-monitor/commit/5af8b08b26390578e5d4f022b46f9a76c6dbb6eb))
* **deps:** update dependency node to v24.18.0 ([aef4101](https://github.com/knowledgepixels/nanopub-monitor/commit/aef4101e82881e929c39c6679d1d208605e73e7b))
* **deps:** update dependency node to v24.18.1 ([e162e8a](https://github.com/knowledgepixels/nanopub-monitor/commit/e162e8a67958f4e898fe11e39d9906e90e362d20))
* **release:** replace branch update script with knowledgepixels/sync-release-artifacts-action ([d226e4c](https://github.com/knowledgepixels/nanopub-monitor/commit/d226e4cba68e26c102bc6f0466f4a5313d0d6742))

### General maintenance

* **logging:** add/enhance logging messages ([95d82c8](https://github.com/knowledgepixels/nanopub-monitor/commit/95d82c871765f2d43b47c0f8421f8f08cca80019))
* setting next snapshot version [skip ci] ([a44c4f8](https://github.com/knowledgepixels/nanopub-monitor/commit/a44c4f8e28bbb13cc9556ebaa3f414c14f533a78))

## [1.5.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.4.0...nanopub-monitor-1.5.0) (2026-06-22)

### Features

* **monitor:** auto-refresh counts and table via Ajax every 10s ([50de408](https://github.com/knowledgepixels/nanopub-monitor/commit/50de408b546d1d92e07c7161ef1f7837838885d4))
* **scanner:** show Nanodash version from Nanodash-Version header ([e8f4e1f](https://github.com/knowledgepixels/nanopub-monitor/commit/e8f4e1fb3550e3841ce45ef01c8123b2a7321ae5))

### Dependency updates

* **core-deps:** update org.nanopub:nanopub dependency to v1.90.0 ([9240242](https://github.com/knowledgepixels/nanopub-monitor/commit/92402421564e3d53efb779a222b70bde1116ab33))
* **core-deps:** update slf4j monorepo to v2.0.18 ([be77d88](https://github.com/knowledgepixels/nanopub-monitor/commit/be77d886678d8fb8071f6582aad0882ab53aaf18))
* **core-deps:** update wicket.version to v10.9.1 ([7c68418](https://github.com/knowledgepixels/nanopub-monitor/commit/7c6841885cafb0718955653beb8732e434f46386))
* **deps:** update dependency maven to v3.9.16 ([ec64b30](https://github.com/knowledgepixels/nanopub-monitor/commit/ec64b30c7a7eab2572cc19b3c74e50a788ed86cc))
* **deps:** update eclipse-temurin docker tag to v25 ([15e6275](https://github.com/knowledgepixels/nanopub-monitor/commit/15e6275dbd10520ec8d45018b2bbcde9171f4f21))
* **deps:** update release dependencies to latest version ([8da9be1](https://github.com/knowledgepixels/nanopub-monitor/commit/8da9be1b234142251640e2e1f6208a1c9b19d52b))
* **deps:** update tomcat docker tag to v11.0.22 ([332e10e](https://github.com/knowledgepixels/nanopub-monitor/commit/332e10e1e1c7470a9d2785711f3510609aacbb0f))

### Tests

* **deps:** update junit-framework monorepo to v6.1.0 ([a52a39f](https://github.com/knowledgepixels/nanopub-monitor/commit/a52a39f928e8c0a7322828ad21d90f8921b3a53a))

### Build and continuous integration

* **deps:** lock file maintenance ([f45d9b6](https://github.com/knowledgepixels/nanopub-monitor/commit/f45d9b6f8d44f7223c18052adfd1ce46a4991675))
* **deps:** lock file maintenance ([44e8689](https://github.com/knowledgepixels/nanopub-monitor/commit/44e86892e7f9b1223430f67b407e2b02997cdd0b))
* **deps:** update action actions/setup-java to v5.2.0 ([25d3f84](https://github.com/knowledgepixels/nanopub-monitor/commit/25d3f84254cfef9bf7168397501ac9cf0674eafc))
* **deps:** update actions/checkout action to v6.0.3 ([92a0161](https://github.com/knowledgepixels/nanopub-monitor/commit/92a0161a4fcc6bac0a9def995c9c3a2ba377406b))
* **deps:** update actions/checkout action to v7 ([b5051fe](https://github.com/knowledgepixels/nanopub-monitor/commit/b5051feb7f31e04d5f0d231aeab4069755ca56a0))
* **deps:** update actions/setup-java action to v5.3.0 ([585fc42](https://github.com/knowledgepixels/nanopub-monitor/commit/585fc42dbef0b5d57860caaeda576865821b041d))
* **deps:** update actions/setup-node action to v6 ([21fa015](https://github.com/knowledgepixels/nanopub-monitor/commit/21fa015f520ea441be16375b1fc9175949289229))
* **deps:** update dependency node to v22.23.0 ([9f139d4](https://github.com/knowledgepixels/nanopub-monitor/commit/9f139d4a8d3ae197019c21f95a7b471d896d5cdd))
* **deps:** update dependency node to v24 ([bbc3d80](https://github.com/knowledgepixels/nanopub-monitor/commit/bbc3d80546915df1e0f684ed74c0186e84b42c76))
* **deps:** update jacoco.maven.plugin.version to v0.8.15 ([8f31189](https://github.com/knowledgepixels/nanopub-monitor/commit/8f31189b57d60fbc42bd4e15f2c70dbecd09ce5e))
* **deps:** update maven-compiler-plugin to v3.15.0 ([2261067](https://github.com/knowledgepixels/nanopub-monitor/commit/2261067e6510c4d5c670d49e802f80c4c724fbd6))
* **deps:** update maven.surefire.plugin.version to v3.5.6 ([8fe8939](https://github.com/knowledgepixels/nanopub-monitor/commit/8fe8939135bd5f387391177e1585452c192e810f))

### General maintenance

* add Renovate configuration for update dependency management ([41f2de1](https://github.com/knowledgepixels/nanopub-monitor/commit/41f2de1ac16c6c97515dbe7187815a4a658c7f85))
* **config:** migrate config renovate.json ([ee25c24](https://github.com/knowledgepixels/nanopub-monitor/commit/ee25c2447c5a735b3575ae8a8e0d5d26480d5698))
* move dependency versions to properties section for better management ([29dcb50](https://github.com/knowledgepixels/nanopub-monitor/commit/29dcb507b917a3b63d2b9d55da56cdd07e07bd54))
* **renovate:** enable lock file maintenance ([91328da](https://github.com/knowledgepixels/nanopub-monitor/commit/91328da3d729c51a4fa1c0bce522b36aa35e0695))
* replace source/target with release argument ([674b83a](https://github.com/knowledgepixels/nanopub-monitor/commit/674b83a2e001a462ce977fac4237389c84931610))
* setting next snapshot version [skip ci] ([063066e](https://github.com/knowledgepixels/nanopub-monitor/commit/063066edd28bb80217c0b74d79d510678c5be2ee))

## [1.4.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.3.0...nanopub-monitor-1.4.0) (2026-05-26)

### Features

* reduce scan interval to 10s and throttle ip-api.com lookups ([820931e](https://github.com/knowledgepixels/nanopub-monitor/commit/820931e1ce9bb99f7e3d7cc489492d029d116880))
* **scanner:** gather and show query loaded-nanopub checksum ([49a12be](https://github.com/knowledgepixels/nanopub-monitor/commit/49a12be7008d4aaea3117cac52a558ef4ca8e708))

### General maintenance

* setting next snapshot version [skip ci] ([4e9821c](https://github.com/knowledgepixels/nanopub-monitor/commit/4e9821ccf8680263d405fdabfa032e2d63be167f))

## [1.3.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.2.0...nanopub-monitor-1.3.0) (2026-05-18)

### Features

* **scanner:** show nanopub-server protocolVersion as Version ([3f54db4](https://github.com/knowledgepixels/nanopub-monitor/commit/3f54db4dd6bf2659f5c96a04245f9b0d10bf8b5c))
* **scanner:** use Nanopub-Query-Loaded-Nanopub-Count header for query count ([4964f7b](https://github.com/knowledgepixels/nanopub-monitor/commit/4964f7b760a62014f71c90f90e5087ef852df670))
* **ui:** center main table and cap map width to table width ([99f0f23](https://github.com/knowledgepixels/nanopub-monitor/commit/99f0f233d39eaa3bda3d61364d6e11ca44e700d2))

### General maintenance

* setting next snapshot version [skip ci] ([88e6002](https://github.com/knowledgepixels/nanopub-monitor/commit/88e60023a4314735a00475c7d82fa48c3d7e0c44))

## [1.2.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.1.0...nanopub-monitor-1.2.0) (2026-05-13)

### Features

* **scanner:** parallelize server scan and lower scan-freq to 120s ([4a869a0](https://github.com/knowledgepixels/nanopub-monitor/commit/4a869a02a064a9c11ecabbe1d88b35cd83b4c47d))

### General maintenance

* setting next snapshot version [skip ci] ([876acd1](https://github.com/knowledgepixels/nanopub-monitor/commit/876acd1dfd51785dcd052be2e310275db9d814f3))

## [1.1.0](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.0.1...nanopub-monitor-1.1.0) (2026-05-12)

### Features

* **monitor:** add /.json status endpoint for machine consumers ([e3cddcf](https://github.com/knowledgepixels/nanopub-monitor/commit/e3cddcff15a1a43ebbce4f3f87df3ea26f616a0c))
* **monitor:** expose Nanopub-Monitor-Version header and read it from peers ([d29a229](https://github.com/knowledgepixels/nanopub-monitor/commit/d29a229f7337233ad69b9837dcf497f236ceca3f))
* **monitor:** link to /.json next to /.csv on the page footer ([b7e36e4](https://github.com/knowledgepixels/nanopub-monitor/commit/b7e36e43e1b23b7684503a5e2c054003def9066e))
* **monitor:** show nanopub count and test-instance flag ([d30e59b](https://github.com/knowledgepixels/nanopub-monitor/commit/d30e59b05042658125f7a96c41d183855a14154a))
* **monitor:** show nanopub count for legacy nanopub-server services ([75c8f2e](https://github.com/knowledgepixels/nanopub-monitor/commit/75c8f2ee99f15aca7c7625d9f529e85dc91675ae))
* **monitor:** show registry setting and scope hash consensus to it ([d29451c](https://github.com/knowledgepixels/nanopub-monitor/commit/d29451c16bf084161df8265cee9feb37d3d3116f))
* **monitor:** show server version for registries and queries ([e43645b](https://github.com/knowledgepixels/nanopub-monitor/commit/e43645b93f60526ff900c3f5665262de5b564d22))
* **scanner:** check registry/query Status headers and trust-state agreement ([75bd489](https://github.com/knowledgepixels/nanopub-monitor/commit/75bd489b60db40ce53dbd00d949c6e0a6e638041))

### Dependency updates

* **api-deps:** update org.nanopub:nanopub dependency to v1.86.0 ([af1f33e](https://github.com/knowledgepixels/nanopub-monitor/commit/af1f33ee45c144dbe7c0e4ba7632080dad780439))
* **core-deps:** update dependency com.google.code.gson:gson to v2.13.2 ([bcfb9e4](https://github.com/knowledgepixels/nanopub-monitor/commit/bcfb9e4218a3c6f4d4005659deb31f5bcfed36bd))
* **core-deps:** update dependency org.nanopub:nanopub to v1.84 ([5388d10](https://github.com/knowledgepixels/nanopub-monitor/commit/5388d10bcc3212d283e48ead41daa51d480be8ff))
* **deps:** add org.jacoco:jacoco-maven-plugin dependency v0.8.13 ([4bc57c6](https://github.com/knowledgepixels/nanopub-monitor/commit/4bc57c62b2297d70ff2fecb5a94f1897a2e2acd3))
* **deps:** update com.google.code.gson:gson to v2.14.0 ([99ce1af](https://github.com/knowledgepixels/nanopub-monitor/commit/99ce1af3bc3cead4aef1f5825069e0b44589ed83))
* **deps:** update com.opencsv:opencsv to v5.12.0 ([f947d23](https://github.com/knowledgepixels/nanopub-monitor/commit/f947d234455bc82fdef54598cd767740df9d5570))
* **deps:** update dependency js-yaml to v4.1.1 ([2de1dd7](https://github.com/knowledgepixels/nanopub-monitor/commit/2de1dd72625510041463f6238aa47d4a0c6d640f))
* **deps:** update org.apache.wicket to v10.9.0 ([9b09995](https://github.com/knowledgepixels/nanopub-monitor/commit/9b0999511321b52986d407e88eb4213b374ce0d9))
* **deps:** update org.junit.jupiter:junit-jupiter to v5.14.4 ([54b3f5c](https://github.com/knowledgepixels/nanopub-monitor/commit/54b3f5c8652f8b2d0bb633f733a0429b62b62bc3))
* **deps:** update org.nanopub:nanopub dependency to v1.86.1 ([945ef8f](https://github.com/knowledgepixels/nanopub-monitor/commit/945ef8f8702783e009a59fb371e9138dc8957e39))
* **deps:** update org.nanopub:nanopub dependency to v1.88.0 ([2954041](https://github.com/knowledgepixels/nanopub-monitor/commit/29540418a777f57c00228e3ed950c2c8ded39d88))
* **deps:** update semantic-release to v25.0.0 and semantic-release-preconfigured-conventional commits to v1.1.156 ([2c58727](https://github.com/knowledgepixels/nanopub-monitor/commit/2c587275b264c67df36dc494e95d0c937ab22bf5))

### Build and continuous integration

* add coveralls parallel build configuration ([3bad43b](https://github.com/knowledgepixels/nanopub-monitor/commit/3bad43b79eae38e926cb299db54f72a63d54655e))
* add Maven test and coverage upload workflow ([de0fd8c](https://github.com/knowledgepixels/nanopub-monitor/commit/de0fd8c66bce5d234bb43a8632cd118fac4802dc))
* **deps:** update actions/checkout action to v5.0.1 ([7c256e9](https://github.com/knowledgepixels/nanopub-monitor/commit/7c256e9c05e6513b7a34390cb882965b8d1c802b))
* **deps:** update actions/setup-java action to v5.1.0 ([884b186](https://github.com/knowledgepixels/nanopub-monitor/commit/884b186fbe4f8fd57febede029e702d738c86927))
* **release:** automate master branch update after release ([2c9d876](https://github.com/knowledgepixels/nanopub-monitor/commit/2c9d876ea9a29464e879b8701131fe7381f2a34d))
* update release workflow to trigger on release branch ([b1e22e9](https://github.com/knowledgepixels/nanopub-monitor/commit/b1e22e994248fa31748267f608d3c0c0f90f48ab))

### General maintenance

* add contributing guidelines ([c5ca016](https://github.com/knowledgepixels/nanopub-monitor/commit/c5ca0167d76fd94b9899998e31d05fa5098c2c28))
* **build:** pin maven-surefire-plugin to v3.5.5 ([1817793](https://github.com/knowledgepixels/nanopub-monitor/commit/1817793788deb779e0118893f0d5f4fca186a056))
* **build:** update jacoco-maven-plugin to v0.8.14 ([12f8a9b](https://github.com/knowledgepixels/nanopub-monitor/commit/12f8a9b0c1d29e155a7971cc3c522a9bce63743e))
* **build:** update jib-maven-plugin to v3.5.1 ([60e9f55](https://github.com/knowledgepixels/nanopub-monitor/commit/60e9f5588de615edfcd79c825b729338c8dd58de))
* **build:** update maven-compiler-plugin to v3.14.1 ([0968e41](https://github.com/knowledgepixels/nanopub-monitor/commit/0968e4141f1a247d85b0912873c6cc2b852082cd))
* **build:** update maven-war-plugin to v3.5.1 ([b9bc4f6](https://github.com/knowledgepixels/nanopub-monitor/commit/b9bc4f61240ba682dd52a16ad3299a74ac0c9b99))
* **readme:** update Nanopub Monitor available instances list ([c62b2aa](https://github.com/knowledgepixels/nanopub-monitor/commit/c62b2aac0442f9b5aa7d4291d826f597554f33a4))
* **sem-release:** update configuration for releasing from 'release' branch ([0367acd](https://github.com/knowledgepixels/nanopub-monitor/commit/0367acd4021d2492e0f6656c6a9b30c78f4b89b7))
* setting next snapshot version [skip ci] ([e2b4489](https://github.com/knowledgepixels/nanopub-monitor/commit/e2b448945a6c8736a42666937dd3bb4bb1816042))

## [1.0.1](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.0.0...nanopub-monitor-1.0.1) (2025-10-20)

### Dependency updates

* **core-deps:** update dependency org.nanopub:nanopub to v1.83 ([380ad08](https://github.com/knowledgepixels/nanopub-monitor/commit/380ad08746ef0373707af68be7a27125c083162a))
* **core-deps:** update dependency org.slf4j:slf4j-simple to v2.0.17 ([0765483](https://github.com/knowledgepixels/nanopub-monitor/commit/076548385c923eb09d57b5d7c549ebba93c0480c))
* **deps:** add com.google.cloud.tools:jib-maven-plugin dependency ([f4a90ea](https://github.com/knowledgepixels/nanopub-monitor/commit/f4a90ead6394549e3ab54f288e3cf6429ae9ee60))
* **deps:** add semantic-release dependencies ([efc760b](https://github.com/knowledgepixels/nanopub-monitor/commit/efc760b07dda9b154ee629b9b7dfb12912404bd3))
* **deps:** update dependency org.eclipse.jetty:jetty-maven-plugin to v11.0.26 ([b5b0838](https://github.com/knowledgepixels/nanopub-monitor/commit/b5b0838c7be5ca9fec59871c96abe039db85b85b))
* **deps:** update Maven to v3.9.11 ([e6c1ce6](https://github.com/knowledgepixels/nanopub-monitor/commit/e6c1ce689a592b3cd848f807e13b3d905dd87b27))

### Bug Fixes

* **ServerList:** update API call to use QueryRef for service retrieval ([24feb24](https://github.com/knowledgepixels/nanopub-monitor/commit/24feb24d8cb6b8b3230c018dcc96ab25dd902984))

### Documentation

* add javadoc annotations ([477e3ef](https://github.com/knowledgepixels/nanopub-monitor/commit/477e3efe827976f44d2504285e79bbe304cc5bda))

### Tests

* **deps:** add dependency org.junit.jupiter:junit-jupiter v5.13.4 ([500d8bd](https://github.com/knowledgepixels/nanopub-monitor/commit/500d8bd5735e0923fd34cba0098ab433a34c44f7))
* **NanopubService:** add unit tests ([d491056](https://github.com/knowledgepixels/nanopub-monitor/commit/d4910562968dea36eb6d53a7711a2ac9d307931c))
* **ServerIpInfo:** add unit tests ([106d84e](https://github.com/knowledgepixels/nanopub-monitor/commit/106d84e3d03892244e01cb2cc149d6618fc63de3))

### Build and continuous integration

* add autorelease workflow config ([bd28ff9](https://github.com/knowledgepixels/nanopub-monitor/commit/bd28ff948242a51108de548d4f7dd59eff6aea85))
* add maven test configuration ([e21f11b](https://github.com/knowledgepixels/nanopub-monitor/commit/e21f11b4735ccefaa7b061057195e6f595d5f075))
* **deps:** update action actions/checkout to v5.0.0 ([dba11ee](https://github.com/knowledgepixels/nanopub-monitor/commit/dba11eebec3f119c8befeb3d829ade733c665285))
* remove maven-test workflow since tests are executed now in the autorelease one ([6cee16a](https://github.com/knowledgepixels/nanopub-monitor/commit/6cee16addbabd5d43126a1dbd8ae8244816dba4d))
* update autorelease configuration to use env vars for Docker credentials and image name ([cfab7a9](https://github.com/knowledgepixels/nanopub-monitor/commit/cfab7a9da43997a9015cbe6b7c6877229881f5c0))

### General maintenance

* add Jetty server configuration files for development/testing ([f1e0cf4](https://github.com/knowledgepixels/nanopub-monitor/commit/f1e0cf47109f87b00cabb1fcae4935909bae64e3))
* add Maven settings for Docker registry authentication ([9dea8e1](https://github.com/knowledgepixels/nanopub-monitor/commit/9dea8e1601b5c9d0553011399b5b79f36881748c))
* add Maven wrapper ([d7ae60b](https://github.com/knowledgepixels/nanopub-monitor/commit/d7ae60b654f61246e256bc6c62006ee9a3184785))
* add semantic-release configuration ([635c874](https://github.com/knowledgepixels/nanopub-monitor/commit/635c87410e5eee6fc518619a8a49a5fc2452690b))
* **docker-compose:** update port mapping syntax ([6f3563d](https://github.com/knowledgepixels/nanopub-monitor/commit/6f3563d62938ce144d41bc2b516e2e77ad2ebe40))
* **Dockerfile:** simplify build process and update base images ([c8b6fdd](https://github.com/knowledgepixels/nanopub-monitor/commit/c8b6fdd17af22324d436e678fb02ce231ef1524d))
* **logging:** update messages for better error handling ([76ec3ba](https://github.com/knowledgepixels/nanopub-monitor/commit/76ec3ba2df4f7e5b5cd0284017cde018bd79364d))
* update maven-compiler-plugin to use Java 21 ([ea47959](https://github.com/knowledgepixels/nanopub-monitor/commit/ea47959e0bd4704673c655666b81df09163baff6))
* update readme ([5d04b94](https://github.com/knowledgepixels/nanopub-monitor/commit/5d04b94656859efe47e6eecb56bddb10b1bc0221))
* update semantic-release configuration for Maven docker release integration ([aee4498](https://github.com/knowledgepixels/nanopub-monitor/commit/aee44989807485308f447c1ab515fce25bf535ac))
* update version to 1.0.1-SNAPSHOT to comply with semantic versioning syntax and add project description ([46797df](https://github.com/knowledgepixels/nanopub-monitor/commit/46797df16e7e0e446d523520ec45dbd5c668f1af))

### Refactoring

* replace tabs with spaces ([c8b62f5](https://github.com/knowledgepixels/nanopub-monitor/commit/c8b62f500b6d566855c880cce87286193ec40312))
* **ServerData:** replace deprecated URL constructor ([b8a9751](https://github.com/knowledgepixels/nanopub-monitor/commit/b8a97519636171a43789fc36f87c82937d3e57c3))

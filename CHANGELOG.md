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

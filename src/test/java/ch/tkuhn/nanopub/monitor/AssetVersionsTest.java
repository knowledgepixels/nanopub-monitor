package ch.tkuhn.nanopub.monitor;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AssetVersionsTest {

    private static final Map<String, String> VERSIONS = Map.of("style.css", "abc", "js/map.js", "def", "favicon.svg", "123");

    private static String apply(String markup) {
        return AssetVersions.apply(markup, VERSIONS::get);
    }

    @Test
    void stylesheetsScriptsAndImagesGetTheirVersion() {
        assertEquals("<link rel=\"stylesheet\" href=\"style.css?v=abc\"/><script src=\"js/map.js?v=def\" defer></script>"
                        + "<link rel=\"icon\" href=\"favicon.svg?v=123\">",
                apply("<link rel=\"stylesheet\" href=\"style.css\"/><script src=\"js/map.js\" defer></script>"
                        + "<link rel=\"icon\" href=\"favicon.svg\">"));
    }

    @Test
    void aVersionWrittenByHandIsReplaced() {
        assertEquals("<link href=\"style.css?v=abc\"/>", apply("<link href=\"style.css?v=8\"/>"));
    }

    @Test
    void referencesToAnythingElseAreLeftAlone() {
        String markup = "<a href=\".csv\">csv</a><a href=\"https://example.org/style.css\">x</a>"
                + "<script src=\"unknown.js\"></script><script src=\"../style.css\"></script><a href=\"#style.css\">y</a>";
        assertEquals(markup, apply(markup));
    }

    @Test
    void theVersionFollowsTheContent() throws IOException {
        String version = version("body { color: red }");
        assertEquals(10, version.length());
        assertEquals(version, version("body { color: red }"));
        assertNotEquals(version, version("body { color: blue }"));
    }

    private static String version(String content) throws IOException {
        return AssetVersions.versionOf(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

}

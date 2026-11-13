package ch.tkuhn.nanopub.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cache busting for the static assets a page refers to: every reference to a stylesheet, script
 * or image of the web application gets a version derived from the file's content appended, so a
 * browser fetches a changed file again and keeps using an unchanged one. The versions are added
 * to the rendered markup, so no reference in a page needs to be maintained by hand.
 */
final class AssetVersions {

    /**
     * A reference to a local asset: an href or src attribute naming a .css, .js or .svg file,
     * with any query string it already carries.
     */
    private static final Pattern ASSET_REFERENCE =
            Pattern.compile("\\b(href|src)=\"([\\w./-]+\\.(?:css|js|svg))(?:\\?[^\"]*)?\"");

    private static final int VERSION_LENGTH = 10;

    private AssetVersions() {
    }

    /**
     * Append a version to every asset reference in the given markup.
     *
     * @param markup    the rendered markup
     * @param versionOf gives the version of the asset at a path relative to the web application
     *                  root, or null if there is no such asset, which leaves the reference as it is
     * @return the markup with versioned asset references
     */
    static String apply(CharSequence markup, Function<String, String> versionOf) {
        Matcher m = ASSET_REFERENCE.matcher(markup);
        StringBuilder result = new StringBuilder(markup.length() + 128);
        while (m.find()) {
            String path = m.group(2);
            String version = path.contains("..") ? null : versionOf.apply(path);
            String replacement = version == null ? m.group()
                    : m.group(1) + "=\"" + path + "?v=" + version + "\"";
            m.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(result);
        return result.toString();
    }

    /**
     * Derive a version from an asset's content.
     *
     * @param content the content of the asset, which is read to its end but not closed
     * @return the first characters of the content's SHA-256 hash in hexadecimal
     * @throws IOException if the content cannot be read
     */
    static String versionOf(InputStream content) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        new DigestInputStream(content, digest).transferTo(java.io.OutputStream.nullOutputStream());
        return HexFormat.of().formatHex(digest.digest()).substring(0, VERSION_LENGTH);
    }

}

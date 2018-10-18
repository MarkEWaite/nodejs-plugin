/*
 * The MIT License
 *
 * Copyright (c) 2018, Nikolas Falco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.nodejs.configfiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NpmrcTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File file;

    @Before
    public void setUp() throws IOException {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("npmrc.config");
            file = folder.newFile(".npmrc");
            hudson.util.IOUtils.copy(is, file);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test
    public void testLoad() throws Exception {
        Npmrc npmrc = Npmrc.load(file);
        assertTrue(npmrc.contains("always-auth"));
        assertEquals("true", npmrc.get("always-auth"));
        assertEquals("\"/var/lib/jenkins/tools/jenkins.plugins.nodejs.tools.NodeJSInstallation/Node_6.x\"",
                npmrc.get("prefix"));
    }

    @Test
    public void testAvoidParseError() throws Exception {
        Npmrc npmrc = Npmrc.load(file);
        assertFalse(npmrc.contains("browser"));
    }

    @Test
    public void testSave() throws Exception {
        String testKey = "test";
        String testValue = "value";

        Npmrc npmrc = Npmrc.load(file);
        npmrc.set(testKey, testValue);
        npmrc.save(file);

        // reload content
        npmrc = Npmrc.load(file);
        assertTrue(npmrc.contains(testKey));
        assertEquals(testValue, npmrc.get(testKey));
    }

    @Test
    public void testCommandAtLast() throws Exception {
        String comment = "test comment";

        Npmrc npmrc = Npmrc.load(file);
        npmrc.addComment(comment);
        npmrc.save(file);

        try (InputStream is = new FileInputStream(file)) {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            assertEquals(';' + comment, lines.get(lines.size() - 1));
        }
    }

}
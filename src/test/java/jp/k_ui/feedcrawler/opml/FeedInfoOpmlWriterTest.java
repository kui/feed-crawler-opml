package jp.k_ui.feedcrawler.opml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import jp.k_ui.feedcrawler.FeedInfo;

import org.junit.Before;
import org.junit.Test;

import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;

public class FeedInfoOpmlWriterTest {
  FeedInfoOpmlWriter writer;

  @Before
  public void buildTargets() {
    writer = new FeedInfoOpmlWriter();
  }

  @Test
  public void testBuildOpmlWithNegativeDepth() throws Exception {
    Collection<FeedInfo> stub = stubFeedInfoCollection();
    Opml opml = writer.buildOpml("stub title", stub, -1);

    assertEquals("stub title", opml.getTitle());

    @SuppressWarnings("unchecked")
    List<Outline> outlines = opml.getOutlines();
    assertContainsTitle("news", outlines);
    assertContainsTitle("it", outlines);
    assertContainsTitle("1st feed", outlines);
    assertContainsTitle("2nd feed", outlines);
    assertContainsTitle("3rd feed", outlines);
  }

  @Test
  public void testBuildOpmlWithOneDepth() throws Exception {
    Collection<FeedInfo> stub = stubFeedInfoCollection();
    Opml opml = writer.buildOpml("stub title", stub, 1);

    assertEquals("stub title", opml.getTitle());

    @SuppressWarnings("unchecked")
    List<Outline> outlines = opml.getOutlines();
    assertContainsTitle("news", outlines);
    assertNotContainsTitle("it", outlines);
    assertContainsTitle("1st feed", outlines);
    assertContainsTitle("2nd feed", outlines);
    assertContainsTitle("3rd feed", outlines);
  }

  @Test
  public void testBuildOpmlWithZeroDepth() throws Exception {
    Collection<FeedInfo> stub = stubFeedInfoCollection();
    Opml opml = writer.buildOpml("stub title", stub, 0);

    assertEquals("stub title", opml.getTitle());

    @SuppressWarnings("unchecked")
    List<Outline> outlines = opml.getOutlines();
    assertNotContainsTitle("news", outlines);
    assertNotContainsTitle("it", outlines);
    assertContainsTitle("1st feed", outlines);
    assertContainsTitle("2nd feed", outlines);
    assertContainsTitle("3rd feed", outlines);
  }

  @Test
  public void printWriteWriter() throws Exception {
    Writer w = new OutputStreamWriter(System.out);
    System.out.println("\n# categoriesDepth: 1");
    writer.write(w, "stub title", stubFeedInfoCollection(), 1);
    System.out.println("\n# categoriesDepth: 0");
    writer.write(w, "stub title", stubFeedInfoCollection(), 0);
    System.out.println("\n# categoriesDepth: -1");
    writer.write(w, "stub title", stubFeedInfoCollection(), -1);
  }

  private void assertContainsTitle(String title, List<Outline> outlines) {
    assertTrue(
        "title: \"" + title + "\" was contained",
        containsTitle(title, outlines));
  }

  private void assertNotContainsTitle(String title, List<Outline> outlines) {
    assertFalse(
        "title: \"" + title + "\" was not contained",
        containsTitle(title, outlines));
  }

  @SuppressWarnings("unchecked")
  private boolean containsTitle(String title, List<Outline> outlines) {
    for (Outline o : outlines)
      if (title.equals(o.getTitle()) || containsTitle(title, o.getChildren()))
        return true;
    return false;
  }

  private Collection<FeedInfo> stubFeedInfoCollection() throws MalformedURLException {
    Collection<FeedInfo> infoCollection = new HashSet<>();

    FeedInfo i = new FeedInfo();
    i.setTitle("1st feed");
    i.setFeedUrl(new URL("http://bar.com/feed"));
    i.setHtmlUrl(new URL("http://bar.com"));
    infoCollection.add(i);

    i = new FeedInfo();
    i.setTitle("2nd feed");
    i.setFeedUrl(new URL("http://foo.com/feed"));
    i.setHtmlUrl(new URL("http://foo.com"));
    i.setCategories(Arrays.asList("news"));
    infoCollection.add(i);

    i = new FeedInfo();
    i.setTitle("3rd feed");
    i.setFeedUrl(new URL("https://hoge.com/feed"));
    i.setHtmlUrl(new URL("https://hoge.com"));
    i.setCategories(Arrays.asList("news", "it"));
    infoCollection.add(i);

    return infoCollection;
  }
}

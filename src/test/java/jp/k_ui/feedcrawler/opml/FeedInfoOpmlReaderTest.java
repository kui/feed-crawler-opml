package jp.k_ui.feedcrawler.opml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.k_ui.feedcrawler.FeedInfo;

import org.junit.Before;
import org.junit.Test;

import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;

public class FeedInfoOpmlReaderTest {
  FeedInfoOpmlReader reader;

  @Before
  public void buildTargets() {
    reader = new FeedInfoOpmlReader();
  }

  @Test
  public void testReadOpml() throws MalformedURLException {
    Opml opml = stubNormalOpml();

    Collection<FeedInfo> info = reader.read(opml);

    assertEquals(3, info.size());

    assertContainsTitle("1st feed", info);
    assertContainsTitle("2nd feed", info);
    assertContainsTitle("3rd feed", info);

    assertContainsFeedUrl("http://bar.com/feed", info);
    assertContainsFeedUrl("https://hoge.com/feed", info);
    assertContainsFeedUrl("http://foo.com/feed", info);

    assertContainsCategories(info, "news");
    assertContainsCategories(info, "news", "it");
    assertContainsCategories(info);
  }

  @Test
  public void testReadFile() throws Exception {
    URL feedXmlUrl = ClassLoader.getSystemClassLoader().getResource("feed.xml");
    File feedXml = new File(feedXmlUrl.toURI());
    Collection<FeedInfo> info = reader.read(feedXml);

    assertContainsTitle("1st feed", info);
    assertContainsTitle("2nd feed", info);
    assertContainsTitle("3rd feed", info);

    assertContainsFeedUrl("http://bar.com/feed", info);
    assertContainsFeedUrl("https://hoge.com/feed", info);
    assertContainsFeedUrl("http://foo.com/feed", info);

    assertContainsCategories(info, "news");
    assertContainsCategories(info, "news", "it");
    assertContainsCategories(info);
  }

  private void assertContainsCategories(Collection<FeedInfo> info, String... categoriesArray) {
    if (categoriesArray.length == 0) {
      for (FeedInfo i : info)
        if (i.getCategories() == null || i.getCategories().isEmpty())
          return;
      fail("categories: null was not contained");
    } else {
      List<String> categories = Arrays.asList(categoriesArray);
      for (FeedInfo i : info)
        if (categories.equals(i.getCategories()))
          return;
      fail("categories:" + categories + " was not contained");
    }
  }

  private void assertContainsFeedUrl(String urlString, Collection<FeedInfo> info)
      throws MalformedURLException {
    URL url = new URL(urlString);
    for (FeedInfo i : info)
      if (url.equals(i.getFeedUrl()))
        return;
    fail("feedUrl:\"" + url + "\" was not contained");
  }

  private void assertContainsTitle(String title, Collection<FeedInfo> info) {
    for (FeedInfo i : info)
      if (title.equals(i.getTitle()))
        return;
    fail("title:\"" + title + "\" was not contained");
  }

  private Opml stubNormalOpml() throws MalformedURLException {
    Opml opml = new Opml();
    opml.setTitle("stub title");

    List<Outline> outlines = new ArrayList<>();
    Outline o;
    o = new Outline("1st feed", new URL("http://bar.com/feed"), new URL("http://bar.com"));
    o.setText("1st feed");
    outlines.add(o);

    Outline catOut = new Outline();
    catOut.setTitle("news");
    catOut.setText("news");
    outlines.add(catOut);

    Outline subCatOut = new Outline();
    subCatOut.setTitle("it");
    subCatOut.setText("it");

    List<Outline> catChildren = new ArrayList<>();
    o = new Outline("2nd feed", new URL("http://foo.com/feed"), new URL("http://foo.com"));
    o.setText("2nd feed");
    catChildren.add(o);
    catChildren.add(subCatOut);
    catOut.setChildren(catChildren);

    o = new Outline("3rd feed", new URL("https://hoge.com/feed"), new URL("https://hoge.com"));
    o.setText("3rd feed");
    subCatOut.setChildren(Collections.singletonList(o));

    opml.setOutlines(outlines);
    return opml;
  }
}

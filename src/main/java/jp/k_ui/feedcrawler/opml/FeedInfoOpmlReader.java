package jp.k_ui.feedcrawler.opml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import jp.k_ui.feedcrawler.FeedInfo;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;

public class FeedInfoOpmlReader {
  /**
   * @throws FileNotFoundException
   *           thrown if the file could not be found.
   * @throws IOException
   *           thrown if there is problem reading the file.
   * @throws IllegalArgumentException
   *           thrown if feed type is not a OPML
   * @throws FeedException
   *           thrown if the feed could not be parsed
   * @throws MalformedURLException
   *           thrown if feed "xmlUrl" is invalid formed
   */
  public Collection<FeedInfo> read(File f) throws FileNotFoundException, IllegalArgumentException,
      IOException, FeedException, MalformedURLException {
    WireFeedInput input = new WireFeedInput();
    return castOpml(input.build(f));
  }

  /**
   * @throws IllegalArgumentException
   *           thrown if feed type is not a OPML
   * @throws FeedException
   *           thrown if the feed could not be parsed
   * @throws MalformedURLException
   *           thrown if feed "xmlUrl" is invalid formed
   */
  public Collection<FeedInfo> read(Reader r) throws IllegalArgumentException, FeedException,
      MalformedURLException {
    WireFeedInput input = new WireFeedInput();
    return castOpml(input.build(r));
  }

  /**
   * @throws MalformedURLException
   *           thrown if feed "xmlUrl" is invalid formed
   */
  public Collection<FeedInfo> read(Opml opml) throws MalformedURLException {
    return extractFeedInfo(opml);
  }

  private Collection<FeedInfo> castOpml(WireFeed feed) throws MalformedURLException {
    if (feed instanceof Opml)
      return extractFeedInfo((Opml) feed);
    throw new IllegalArgumentException("expected OPML file: " + feed.getClass());
  }

  private Collection<FeedInfo> extractFeedInfo(Opml opml) throws MalformedURLException {
    Collection<FeedInfo> info = new HashSet<>();
    @SuppressWarnings("unchecked")
    List<Outline> outlines = opml.getOutlines();
    for (Outline o : outlines)
      info.addAll(extractFeedInfo(o, Collections.<String> emptyList()));
    return info;
  }

  private Collection<FeedInfo> extractFeedInfo(Outline outline, List<String> categories)
      throws MalformedURLException {
    if (outline.getXmlUrl() == null) {
      return extractFeedInfoAsCategoryNode(outline, categories);
    } else {
      return extractFeedInfoAsFeedNode(outline, categories);
    }
  }

  private Collection<FeedInfo> extractFeedInfoAsCategoryNode(Outline outline,
      List<String> categories)
      throws MalformedURLException {
    List<String> newCategories = new ArrayList<>(categories.size() + 1);
    newCategories.addAll(categories);
    newCategories.add(outline.getTitle());

    Collection<FeedInfo> info = new HashSet<>();
    @SuppressWarnings("unchecked")
    List<Outline> children = outline.getChildren();
    for (Outline c : children)
      info.addAll(extractFeedInfo(c, newCategories));

    return info;
  }

  private Collection<FeedInfo> extractFeedInfoAsFeedNode(Outline outline, List<String> categories)
      throws MalformedURLException {
    FeedInfo info = new FeedInfo();
    info.setFeedUrl(new URL(outline.getXmlUrl()));
    if (outline.getHtmlUrl() != null)
      info.setHtmlUrl(new URL(outline.getHtmlUrl()));
    info.setTitle(outline.getTitle());
    info.setCategories(categories);
    return Collections.singleton(info);
  }
}

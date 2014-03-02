package jp.k_ui.feedcrawler.opml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.k_ui.feedcrawler.FeedInfo;

import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

public class FeedInfoOpmlWriter {
  /**
   * @param title
   *          OPML title
   * @param feedInfo
   *          OPML source feeds
   * @param categoriesDepth
   *          max categories outline depth or unlimited-depth if negative integer
   * @throws IOException
   *           thrown if there was some problem writing to the Writer.
   * @throws FeedException
   *           thrown if the XML representation for the feed could not be created.
   */
  public void write(Writer writer, String title, Collection<FeedInfo> feedInfo)
      throws IOException, FeedException {
    write(writer, title, feedInfo, 1);
  }

  /**
   * @param title
   *          OPML title
   * @param feedInfo
   *          OPML source feeds
   * @param categoriesDepth
   *          max categories outline depth or unlimited-depth if negative integer
   * @throws IOException
   *           thrown if there was some problem writing to the Writer.
   * @throws FeedException
   *           thrown if the XML representation for the feed could not be created.
   */
  public void write(Writer writer, String title, Collection<FeedInfo> feedInfo, int categoriesDepth)
      throws IOException, FeedException {
    WireFeedOutput o = new WireFeedOutput();
    o.output(buildOpml(title, feedInfo, categoriesDepth), writer);
  }

  /**
   * @param title
   *          OPML title
   * @param feedInfo
   *          OPML source feeds
   * @param categoriesDepth
   *          max categories outline depth or unlimited-depth if negative integer
   * @throws IOException
   *           thrown if there was some problem writing to the File.
   * @throws FeedException
   *           thrown if the XML representation for the feed could not be created.
   */
  public void write(File file, String title, Collection<FeedInfo> feedInfo)
      throws IOException, FeedException {
    write(file, title, feedInfo, 1);
  }

  /**
   * @param title
   *          OPML title
   * @param feedInfo
   *          OPML source feeds
   * @param categoriesDepth
   *          max categories outline depth or unlimited-depth if negative integer
   * @throws IOException
   *           thrown if there was some problem writing to the File.
   * @throws FeedException
   *           thrown if the XML representation for the feed could not be created.
   */
  public void write(File file, String title, Collection<FeedInfo> feedInfo, int categoriesDepth)
      throws IOException, FeedException {
    WireFeedOutput o = new WireFeedOutput();
    o.output(buildOpml(title, feedInfo, categoriesDepth), file);
  }

  /**
   *
   * @param title
   *          OPML title
   * @param feedInfo
   *          OPML source feeds
   * @param categoriesDepth
   *          max categories outline depth or unlimited-depth if negative integer
   * @return
   */
  public Opml buildOpml(String title, Collection<FeedInfo> feedInfo, int categoriesDepth) {
    if (feedInfo == null)
      throw new IllegalArgumentException("feedInfo is expected non-null");

    Opml opml = new Opml();
    opml.setFeedType("opml_1.0");
    opml.setTitle(title);
    opml.setOutlines(buidOutlines(feedInfo, categoriesDepth));
    return opml;
  }

  private List<Outline> buidOutlines(Collection<FeedInfo> feedInfo, int categoriesDepth) {
    List<Outline> outlines = new ArrayList<Outline>();
    for (FeedInfo i : feedInfo)
      addTo(outlines, i, categoriesDepth);
    return outlines;
  }

  private void addTo(List<Outline> outlines, FeedInfo feedInfo, int categoriesDepth) {
    List<String> categories;
    if (categoriesDepth < 0) {
      categories = feedInfo.getCategories();
    } else {
      if (feedInfo.getCategories() == null) {
        categories = null;
      } else {
        categories = feedInfo.getCategories().subList(0, categoriesDepth);
      }
    }
    Outline o = buildFeedOutline(feedInfo);

    if (categories == null || categories.isEmpty()) {
      outlines.add(o);
    } else {
      Outline parent = getCategoryOutlineFrom(outlines, categories);

      @SuppressWarnings("unchecked")
      List<Outline> oldChildren = parent.getChildren();
      List<Outline> newChildren = new ArrayList<Outline>(oldChildren.size() + 1);
      newChildren.addAll(oldChildren);
      newChildren.add(buildFeedOutline(feedInfo));

      parent.setChildren(newChildren);
    }
  }

  private Outline getCategoryOutlineFrom(List<Outline> outlines, List<String> categories) {
    String head = categories.get(0);
    Outline categoryOutline = findCategoryOutlineFrom(outlines, head);
    if (categoryOutline == null) {
      categoryOutline = buildCategoryOutline(head);
      outlines.add(categoryOutline);
    }

    List<String> tailCategories = categories.subList(1, categories.size());
    if (tailCategories.isEmpty())
      return categoryOutline;

    @SuppressWarnings("unchecked")
    List<Outline> children = categoryOutline.getChildren();
    return getCategoryOutlineFrom(children, tailCategories);
  }

  private Outline findCategoryOutlineFrom(List<Outline> outlines, String head) {
    for (Outline o : outlines)
      if (head.equals(o.getTitle()))
        return o;
    return null;
  }

  private Outline buildCategoryOutline(String category) {
    Outline o = new Outline();
    o.setTitle(category);
    o.setText(category);
    return o;
  }

  private Outline buildFeedOutline(FeedInfo feedInfo) {
    Outline o = new Outline(feedInfo.getTitle(), feedInfo.getFeedUrl(), feedInfo.getHtmlUrl());
    o.setType("rss");
    o.setText(feedInfo.getTitle());
    return o;
  }
}

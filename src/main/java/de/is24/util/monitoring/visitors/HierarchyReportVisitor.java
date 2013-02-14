package de.is24.util.monitoring.visitors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import de.is24.util.monitoring.visitors.AbstractSortedReportVisitor.Entry;


/**
 * @author oschmitz
 */
public class HierarchyReportVisitor extends AbstractSortedReportVisitor {
  private static final Logger LOGGER = Logger.getLogger(HierarchyReportVisitor.class);

  private Tree tree;

  public HierarchyReportVisitor() {
    LOGGER.debug("+++ enter HierarchyReportVisitor.HierarchyReportVisitor +++");
    tree = new Tree();
  }

  protected void addEntry(Entry entry) {
    tree.addEntry(entry);
  }

  public Tree getTree() {
    return tree;
  }

  /**
   *
   */

  public String toString() {
    List list = tree.getAllNodesWithEntries();
    StringBuilder buffy = new StringBuilder();
    buffy.append(getClass().getName());
    buffy.append("\n");

    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      Tree.TreeNode element = (Tree.TreeNode) iter.next();
      Iterator entryIterator = element.getEntries();
      while (entryIterator.hasNext()) {
        Entry entry = (Entry) entryIterator.next();
        buffy.append(entry.getValue());
        buffy.append("\n");
      }
    }
    return buffy.toString();
  }

  public static class Tree {
    private TreeNode root;

    protected Tree() {
      root = new TreeNode("", "root");
    }

    public TreeNode getRoot() {
      return root;
    }

    public void addEntry(Entry entry) {
      StringTokenizer toki = new StringTokenizer(entry.getPath(), ".");
      TreeNode currentNode = root;
      while (toki.hasMoreTokens()) {
        String key = toki.nextToken();
        currentNode = currentNode.getChild(key);
      }
      currentNode.addEntry(entry);
    }

    List getAllNodesWithEntries() {
      ArrayList list = new ArrayList();
      root.addAllNodesWithEntries(list);
      return list;
    }


    public static final class TreeNode {
      String name;
      String fqn;
      TreeMap children;
      TreeMap entries;

      private TreeNode(String fqn, String name) {
        this.fqn = fqn;
        this.name = name;
      }

      public TreeNode getChild(String childName) {
        TreeNode child = null;
        if (children != null) {
          child = (TreeNode) children.get(childName);
        }
        if (child == null) {
          child = new TreeNode(fqn + "." + childName, childName);
          if (children == null) {
            children = new TreeMap();
          }
          children.put(childName, child);
        }
        return child;
      }

      public void addEntry(Entry entry) {
        if (entries == null) {
          entries = new TreeMap();
        }
        entries.put(entry.getKey(), entry);
      }

      public boolean hasEntries() {
        return (entries != null) && (entries.size() > 0);
      }

      public Iterator getEntries() {
        return (entries == null) ? null : entries.values().iterator();
      }

      public boolean hasChildren() {
        return (children != null) && (children.size() > 0);
      }

      public List addAllNodesWithEntries(List list) {
        if (this.hasEntries()) {
          list.add(this);
        }
        if (hasChildren()) {
          Iterator iter = children.values().iterator();
          while (iter.hasNext()) {
            TreeNode treeNode = (TreeNode) iter.next();
            treeNode.addAllNodesWithEntries(list);
          }
        }
        return list;
      }
    }
  }

}

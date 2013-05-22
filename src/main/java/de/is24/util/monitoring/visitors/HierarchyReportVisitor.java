package de.is24.util.monitoring.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.apache.log4j.Logger;


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

  @Override
  protected void addEntry(Entry entry) {
    tree.addEntry(entry);
  }

  public Tree getTree() {
    return tree;
  }

  /**
   *
   */

  @Override
  public String toString() {
    StringBuilder buffy = new StringBuilder();
    buffy.append(getClass().getName()).append("\n");

    for (Tree.TreeNode element : tree.getAllNodesWithEntries()) {
      for (Entry entry : element.entries()) {
        buffy.append(entry.getValue()).append("\n");
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

    List<TreeNode> getAllNodesWithEntries() {
      return root.addAllNodesWithEntries(new ArrayList<TreeNode>());
    }


    public static final class TreeNode {
      String name;
      String fqn;
      TreeMap<String, TreeNode> children;
      TreeMap<String, Entry> entries;

      private TreeNode(String fqn, String name) {
        this.fqn = fqn;
        this.name = name;
      }

      public TreeNode getChild(String childName) {
        TreeNode child = null;
        if (children != null) {
          child = children.get(childName);
        }
        if (child == null) {
          child = new TreeNode(fqn + "." + childName, childName);
          if (children == null) {
            children = new TreeMap<String, TreeNode>();
          }
          children.put(childName, child);
        }
        return child;
      }

      public void addEntry(Entry entry) {
        if (entries == null) {
          entries = new TreeMap<String, Entry>();
        }
        entries.put(entry.getKey(), entry);
      }

      public boolean hasEntries() {
        return (entries != null) && (entries.size() > 0);
      }

      public Iterator<Entry> getEntries() {
        return (entries == null) ? null : entries.values().iterator();
      }

      public Collection<Entry> entries() {
        return (entries == null) ? Collections.<Entry>emptyList() : entries.values();
      }

      public boolean hasChildren() {
        return (children != null) && (children.size() > 0);
      }

      public List<TreeNode> addAllNodesWithEntries(List<TreeNode> list) {
        if (this.hasEntries()) {
          list.add(this);
        }
        if (hasChildren()) {
          for (TreeNode treeNode : children.values()) {
            treeNode.addAllNodesWithEntries(list);
          }
        }
        return list;
      }
    }
  }

}

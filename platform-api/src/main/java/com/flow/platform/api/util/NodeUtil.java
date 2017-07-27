/*
 * Copyright 2017 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flow.platform.api.util;

import com.flow.platform.api.domain.Flow;
import com.flow.platform.api.domain.Node;
import com.flow.platform.api.exception.YmlException;
import com.flow.platform.domain.Jsonable;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.yaml.snakeyaml.Yaml;

/**
 * @author yh@firim
 */
public class NodeUtil {

    /**
     * Build node tree structure from yml file
     *
     * @param path file path
     * @return node tree
     */
    public static Node buildFromYml(File path) {
        try {
            String ymlString = Files.toString(path, Charset.forName("UTF-8"));
            return buildFromYml(ymlString);
        } catch (YmlException e) {
            throw e;
        } catch (Throwable ignore) {
            return null;
        }
    }

    /**
     * Build node tree structure from yml string
     *
     * @param yml raw yml string
     * @return
     */
    public static Node buildFromYml(String yml) {
        Yaml yaml = new Yaml();
        Map result = null;

        try {
            result = (Map) yaml.load(yml);
        } catch (Throwable e) {
            throw new YmlException("Illegal yml definition");
        }

        Object content = result.get("flow");

        if (content == null || !(content instanceof List)) {
            throw new YmlException("Illegal yml definition");
        }

        String rawJson = Jsonable.GSON_CONFIG.toJson(content);
        Flow[] flows = Jsonable.GSON_CONFIG.fromJson(rawJson, Flow[].class);

        // current version only support single flow
        if (flows.length > 1) {
            throw new YmlException("Unsupported multiple flows definition");
        }

        buildNodeRelation(flows[0]);
        return flows[0];
    }

    /**
     * find all node
     */
    public static void recurse(final Node root, final Consumer<Node> onNode) {
        for (Object child : root.getChildren()) {
            recurse((Node) child, onNode);
        }
        onNode.accept(root);
    }


    /**
     * find flow node
     */
    public static Node findRootNode(Node node) {
        if (node.getParent() == null) {
            return node;
        }

        return findRootNode(node.getParent());
    }

    /**
     * return List nodes
     */
    public static List<Node> flat(final Node node) {
        final List<Node> flatted = new LinkedList<>();
        recurse(node, flatted::add);
        return flatted;
    }

    /**
     * get prev node from flow
     */
    public static Node prev(Node node) {
        Node root = findRootNode(node);
        List<Node> nodes = flat(root);
        Integer index = -1;
        Node target = null;
        for (int i = 0; i < nodes.size(); i++) {
            Node item = nodes.get(i);
            if (item.getPath() == node.getPath()) {
                index = i;
            }
        }
        if (index >= 1 && index != -1 && index < nodes.size() - 1) {
            target = nodes.get(index - 1);
        }

        return target;
    }

    /**
     * get next node from flow
     */
    public static Node next(Node node) {
        Node root = findRootNode(node);
        List<Node> nodes = flat(root);
        Integer index = -1;
        Node target = null;
        for (int i = 0; i < nodes.size(); i++) {
            Node item = nodes.get(i);
            if (item.getPath() == node.getPath()) {
                index = i;
            }
        }
        if (index <= nodes.size() - 3 && index != -1) {
            target = nodes.get(index + 1);
        }

        if (index == nodes.size() - 1 && nodes.size() != 1) {
            target = nodes.get(0);
        }
        return target;
    }

    /**
     * Build node path, parent, next, prev relation
     */
    private static void buildNodeRelation(Node<? extends Node> root) {
        setNodePath(root);

        List<? extends Node> children = root.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node childNode = children.get(i);
            childNode.setParent(root);
            if (i > 0) {
                childNode.setPrev(children.get(i - 1));
                children.get(i - 1).setNext(childNode);
            }

            buildNodeRelation(childNode);
        }
    }

    private static void setNodePath(Node node) {
        if (node.getParent() == null) {
            node.setPath("/" + node.getName());
            return;
        }
        node.setPath(String.format("%s/%s", node.getParent().getPath(), node.getName()));
    }
}
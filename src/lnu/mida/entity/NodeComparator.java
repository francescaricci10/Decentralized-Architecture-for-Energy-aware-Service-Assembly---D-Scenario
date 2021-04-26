package lnu.mida.entity;

import java.util.Comparator;

public class NodeComparator implements Comparator<GeneralNode> {
	
    @Override
    public int compare(GeneralNode o1, GeneralNode o2) {
        return Long.compare(o1.getID() ,o2.getID());
    }

}

package space.ponyo.dc1.server.server;

import java.util.LinkedList;

class FixedList {

    private final LinkedList<Boolean> mList;

    private final int mLength;

    public FixedList(int mLength) {
        this.mLength = mLength;
        mList = new LinkedList<>();
    }

    public void add(Boolean t) {
        mList.add(0, t);
        if (mList.size() > mLength) {
            mList.removeLast();
        }
    }

    public boolean isAllFalse() {
        boolean hasTrue = mList.stream()
                .anyMatch(aBoolean -> aBoolean);
        return !hasTrue;
    }
}

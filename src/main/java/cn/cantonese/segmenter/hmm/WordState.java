package cn.cantonese.segmenter.hmm;

public enum WordState {
    B(0), M(1), E(2), S(3);

    int state;
    WordState(int state) {
        this.state = state;
    }
}

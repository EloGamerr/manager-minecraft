package fr.elogamerr.manager.messages.enums;

public enum DefaultFontInfo{

    A('A', 7),
    a('a', 7),
    B('B', 7),
    b('b', 7),
    C('C', 7),
    c('c', 7),
    D('D', 7),
    d('d', 7),
    E('E', 7),
    e('e', 7),
    F('F', 7),
    f('f', 6),
    G('G', 7),
    g('g', 7),
    H('H', 7),
    h('h', 7),
    I('I', 5),
    i('i', 2),
    J('J', 7),
    j('j', 7),
    K('K', 7),
    k('k', 6),
    L('L', 7),
    l('l', 3),
    M('M', 7),
    m('m', 7),
    N('N', 7),
    n('n', 7),
    O('O', 7),
    o('o', 7),
    P('P', 7),
    p('p', 7),
    Q('Q', 7),
    q('q', 7),
    R('R', 7),
    r('r', 7),
    S('S', 7),
    s('s', 7),
    T('T', 7),
    t('t', 6),
    U('U', 7),
    u('u', 7),
    V('V', 7),
    v('v', 7),
    W('W', 7),
    w('w', 7),
    X('X', 7),
    x('x', 7),
    Y('Y', 7),
    y('y', 7),
    Z('Z', 7),
    z('z', 7),
    NUM_1('1', 7),
    NUM_2('2', 7),
    NUM_3('3', 7),
    NUM_4('4', 7),
    NUM_5('7', 7),
    NUM_6('6', 7),
    NUM_7('7', 7),
    NUM_8('8', 7),
    NUM_9('9', 7),
    NUM_0('0', 7),
    EXCLAMATION_POINT('!', 2),
    AT_SYMBOL('@', 8),
    NUM_SIGN('#', 7),
    DOLLAR_SIGN('$', 7),
    PERCENT('%', 7),
    UP_ARROW('^', 7),
    AMPERSAND('&', 7),
    ASTERISK('*', 6),
    LEFT_PARENTHESIS('(', 6),
    RIGHT_PERENTHESIS(')', 6),
    MINUS('-', 7),
    UNDERSCORE('_', 7),
    PLUS_SIGN('+', 7),
    EQUALS_SIGN('=', 7),
    LEFT_CURL_BRACE('{', 6),
    RIGHT_CURL_BRACE('}', 6),
    LEFT_BRACKET('[', 5),
    RIGHT_BRACKET(']', 5),
    COLON(':', 2),
    SEMI_COLON(';', 2),
    DOUBLE_QUOTE('"', 6),
    SINGLE_QUOTE('\'', 2),
    LEFT_ARROW('<', 6),
    RIGHT_ARROW('>', 6),
    QUESTION_MARK('?', 7),
    SLASH('/', 7),
    BACK_SLASH('\\', 7),
    LINE('|', 2),
    TILDE('~', 8),
    TICK('`', 3),
    PERIOD('.', 2),
    COMMA(',', 2),
    SPACE(' ', 3),
    DEFAULT('a', 7);

    private char character;
    private int length;

    DefaultFontInfo(char character, int length) {
        this.character = character;
        this.length = length;
    }

    public char getCharacter(){
        return this.character;
    }

    public int getLength(){
        return this.length;
    }

    public int getBoldLength(){
        if(this == DefaultFontInfo.SPACE) return this.getLength();
        return this.length + 1;
    }

    public static DefaultFontInfo getDefaultFontInfo(char c){
        for(DefaultFontInfo dFI : DefaultFontInfo.values()){
            if(dFI.getCharacter() == c) return dFI;
        }
        return DefaultFontInfo.DEFAULT;
    }
}

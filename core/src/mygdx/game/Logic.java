package mygdx.game;

import java.util.Random;

public class Logic {
    private int N;
    private boolean[][] data;
    private boolean[][] prob;
    private int cnt;
    private int level;
    private int hint_row;
    private int hint_col;

    public Logic(final int N) {
        this.N = N;
        this.data = new boolean[N][N];
        this.prob = new boolean[N][N];
        this.cnt = 0;
        this.level = 0;
    }

    private int randint(int a, int b) {
        Random rnd = new Random();
        return rnd.nextInt(b - a + 1) + a;
    }

    public void new_prob() {
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++)
                this.prob[r][c] = false;

        int rnd = randint(1, N * N / 2);
        if (rnd % 2 == 0) rnd++;
        System.out.printf("rnd: %d\n", rnd);

        for (int i = 0; i < rnd; i++) {
            int r = randint(0, N - 1);
            int c = randint(0, N - 1);
            this.__flip(this.prob, r, c);
        }

        this.reset_game();
    }

    private void __flip(boolean[][] array, int row, int col) {
        array[row][col] = !(array[row][col]);
        if (row >= 1)
            array[row - 1][col] = !(array[row - 1][col]);
        if (col >= 1)
            array[row][col - 1] = !(array[row][col - 1]);
        if (row + 1 < N)
            array[row + 1][col] = !(array[row + 1][col]);
        if (col + 1 < N)
            array[row][col + 1] = !(array[row][col + 1]);
    }

    public void user_flip(int row, int col) {
        this.__flip(this.data, row, col);
        this.cnt++;
    }

    public void hint_flip() {
        System.out.println("hint flip");
        this.data[hint_row][hint_col] = !(this.data[hint_row][hint_col]);
    }

    private int get(int[][] flip, int x, int y) {
        final int[] dx = new int[]{0, -1, 0, 1, 0};
        final int[] dy = new int[]{-1, 0, 0, 0, 1};

        int c = ((data[x][y]) ? 1 : 0);
        for (int d = 0; d < 5; d++) {
            int nx = x + dx[d], ny = y + dy[d];
            if (0 <= nx && nx < N && 0 <= ny && ny < N) {
                c += flip[nx][ny];
            }
        }

        return c % 2;
    }

    private int calc(int[][] flip) {
        for (int r = 1; r < N; r++)
            for (int c = 0; c < N; c++)
                if (this.get(flip, r - 1, c) != 0)
                    flip[r][c] = 1;

        for (int col = 0; col < N; col++)
            if (get(flip, N-1, col) != 0)
                return -1;

        int res = 0;
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++)
                res += flip[r][c];

        return res;
    }

    public void gen_hint() {
        int[][] opt = new int[N][N];
        int[][] flip = new int[N][N];

        int result = -1;
        for (int i = 0; i < (1 << N); i++) {
            for (int r = 0; r < N; r++)
                for (int c = 0; c < N; c++)
                    flip[r][c] = 0;

            for (int c = 0; c < N; c++)
                flip[0][N - c - 1] = (i >> c) & 1;

            int num = calc(flip);
            if (num >= 0 && (result < 0 || result > num)) {
                result = num;
                for (int r = 0; r < N; r++)
                    for (int c = 0; c < N; c++)
                        opt[r][c] = flip[r][c];
            }
        }

        System.out.printf("%d\n", result);

        if (result < 0) {
            System.out.println("That's Impossible!");
        }
        else {
            int rnd = randint(1, result);
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {

                    if (opt[r][c] == 1) {
                        rnd--;

                        if (rnd == 0) {
                            this.hint_row = r;
                            this.hint_col = c;
                            System.out.printf("%d, %d\n", hint_row, hint_col);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void reset_game() {
        this.cnt = 0;
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++)
                this.data[r][c] = this.prob[r][c];
    }

    public void next_game() {
        this.new_prob();
        this.level++;
    }

    public void prev_game() {
        if (this.level == 1) return;

        this.new_prob();
        this.level--;
    }

    public boolean at(int row, int col) {
        return data[row][col];
    }

    public int get_cnt() {
        return cnt;
    }

    public int get_level() {
        return level;
    }

    public boolean is_over() {
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++)
                if (data[r][c] == true)
                    return false;
        return true;
    }

    public String toString() {
        String res = "";
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                res += (data[r][c] ? 1 : 0) + " ";
            }
            res += "\n";
        }
        return res;
    }
}

package mygdx.game;

import mygdx.game.Logic;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class MyGame extends ApplicationAdapter {
	private Stage stage;
	private OrthographicCamera camera;
	private int W;
	private int H;

	private Texture img_replay;
	private Texture img_hint;
	private Texture img_next;
	private Texture img_prev;
	private Texture img_on;
	private Texture img_off;

	private Image btn_replay;
	private Image btn_hint;
	private Image btn_next;
	private Image btn_prev;

	private Image[][] grids;
	private SpriteDrawable drw_on;
	private SpriteDrawable drw_off;
	private int grids_start_x;
	private int grids_start_y;
	private int grid_w;

	private Label lbl_cnt;
	private Label lbl_level;

	private Logic logic;
	private int N;
	private boolean in_action_hint;

	private void init_textures() {
		img_replay = new Texture(Gdx.files.internal("replay.png"));
		img_hint = new Texture(Gdx.files.internal("hint.png"));
		img_next = new Texture(Gdx.files.internal("next.png"));
		img_prev = new Texture(Gdx.files.internal("prev.png"));
		img_on = new Texture(Gdx.files.internal("on.png"));
		img_off = new Texture(Gdx.files.internal("off.png"));
	}

	private void init_menu() {
		// btn_hint
		btn_hint = new Image(img_hint);
		btn_hint.setPosition(W - 100, H - 44);
		btn_hint.setSize(36, 36);
		final RunnableAction[] ra = new RunnableAction[4];
		for (int i = 0; i < 4; i++) {
			ra[i] = new RunnableAction() {
				@Override
				public void run() {
					logic.hint_flip();
					update_grids_img();
				}
			};
		}
		final RunnableAction ca = new RunnableAction() {
			@Override
			public void run() {
				System.out.println("hint over");
				in_action_hint = false;
			}
		};
		btn_hint.addListener(new ClickListener() {
			public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
				return true;
			}
			public void touchUp(InputEvent e, float x, float y, int p, int b) {
				if (in_action_hint == false)
					logic.gen_hint();
				in_action_hint = true;
				stage.addAction(Actions.sequence(ra[0], Actions.delay(0.3f), ra[1], Actions.delay(0.3f), ra[2], Actions.delay(0.3f), ra[3], ca));
				update_grids_img();
				update_menu();
			}
		});
		stage.addActor(btn_hint);

		// btn_replay
		btn_replay = new Image(img_replay);
		btn_replay.setPosition(W - 50, H - 44);
		btn_replay.setSize(36, 36);
		btn_replay.addListener(new ClickListener() {
			public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
				return true;
			}
			public void touchUp(InputEvent e, float x, float y, int p, int b) {
				logic.reset_game();
				update_grids_img();
				update_menu();
			}
		});
		stage.addActor(btn_replay);

		// cnt label
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("UbuntuMono-R.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 28;
		parameter.kerning = false;
		BitmapFont font = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		LabelStyle ls_cnt = new LabelStyle(font, Color.SKY);
		lbl_cnt = new Label("00", ls_cnt);
		lbl_cnt.setPosition(W - 155, H - 40);
		stage.addActor(lbl_cnt);

		LabelStyle ls_level = new LabelStyle(font, Color.GOLDENROD);
		lbl_level = new Label("Level 1", ls_level);
		lbl_level.setPosition(55, H - 40);
		stage.addActor(lbl_level);

		// btn_prev
		btn_prev = new Image(img_prev);
		btn_prev.setPosition(5, H - 50);
		btn_prev.setSize(48, 48);
		btn_prev.addListener(new ClickListener() {
			public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
				return true;
			}
			public void touchUp(InputEvent e, float x, float y, int p, int b) {
				logic.prev_game();
				update_grids_img();
				update_menu();
			}
		});
		stage.addActor(btn_prev);

		// btn_next
		btn_next = new Image(img_next);
		btn_next.setPosition(155, H - 50);
		btn_next.setSize(48, 48);
		btn_next.addListener(new ClickListener() {
			public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
				return true;
			}
			public void touchUp(InputEvent e, float x, float y, int p, int b) {
				logic.next_game();
				update_grids_img();
				update_menu();
			}
		});
		stage.addActor(btn_next);
	}

	private void init_grids() {
		this.grids = new Image[N][N];
		this.grid_w = 70;
		this.grids_start_x = (W - N * grid_w) / 2;
		this.grids_start_y = 20;

		this.drw_on = new SpriteDrawable(new Sprite(this.img_on));
		this.drw_off = new SpriteDrawable(new Sprite(this.img_off));

		for (int r = 0; r < N; r++) {
			for (int c = 0; c < N; c++) {
				this.grids[r][c] = new Image();
				int pos_x = grids_start_x + c * grid_w;
				int pos_y = grids_start_y + r * grid_w;
				this.grids[r][c].setPosition(pos_x, pos_y);
				this.grids[r][c].setSize(64f, 64f);

				final int row = r;
				final int col = c;
				this.grids[r][c].addListener(new ClickListener() {
					public boolean touchDown(InputEvent e, float x, float y, int p, int b) {
						return true;
					}
					public void touchUp(InputEvent e, float x, float y, int p, int b) {
						System.out.printf("grid %d, %d\n", col, row);

						logic.user_flip(row, col);
						update_grids_img();
						update_menu();
						check_over();
					}
				});

				this.stage.addActor(this.grids[r][c]);
			}
		}

		this.update_grids_img();
	}

	private void update_grids_img() {
		for (int r = 0; r < N; r++) {
			for (int c = 0; c < N; c++) {
				if (this.logic.at(r, c) == true)
					this.grids[r][c].setDrawable(this.drw_on);
				else
					this.grids[r][c].setDrawable(this.drw_off);
			}
		}
	}

	private void update_menu() {
		this.lbl_cnt.setText(String.format("%02d", this.logic.get_cnt()));
		this.lbl_level.setText("Level " + this.logic.get_level());
	}

	private void check_over() {
		if (!this.logic.is_over()) return;

		System.out.println("over");

		RunnableAction ra = new RunnableAction() {
			@Override
			public void run() {
				// System.out.println("r");
				logic.next_game();
				update_grids_img();
				update_menu();
			}
		};

		stage.addAction(Actions.sequence(Actions.fadeOut(1.5f), ra, Actions.fadeIn(1.0f)));
	}

	@Override
	public void create () {
		camera = new OrthographicCamera();
		stage = new Stage(new FillViewport(380, 420, camera));
		W = Gdx.graphics.getWidth();
		H = Gdx.graphics.getHeight();
		Gdx.input.setInputProcessor(stage);

		N = 5;
		this.in_action_hint = false;
		logic = new Logic(N);
		logic.next_game();

		this.init_textures();
		this.init_menu();
		this.init_grids();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}
}

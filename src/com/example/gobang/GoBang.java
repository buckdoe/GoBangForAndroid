package com.example.gobang;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class GoBang extends ActionBarActivity {

	public static int LINE_NUM = 15;
	public static int GRID_NUM = LINE_NUM - 1;
	public static int MARGIN_SIZE = 50;
	public static int TUPLE_NUM = ((LINE_NUM - 5 + 1) * LINE_NUM * 2 + 2 * ((1 + LINE_NUM - 5 + 1)
			* (LINE_NUM - 5 + 1) / 2 + (1 + LINE_NUM - 1 - 5 + 1)
			* (LINE_NUM - 1 - 5 + 1) / 2));

	public enum COLOR_TYPE {
		COLOR_WHITE, COLOR_BLACK, COLOR_BUTT
	};

	class GRID_POINT_INFO {
		boolean IsHasGoBang;
		COLOR_TYPE eColorType;
		Point ptGridPoint;
		int nScore;

		GRID_POINT_INFO() {
			IsHasGoBang = false;
			eColorType = COLOR_TYPE.COLOR_BUTT;
			ptGridPoint = new Point();
			nScore = -1;
		}
	};

	public enum TUPLE_STATE {
		TUPLE_STATE_BLANK, TUPLE_STATE_B, TUPLE_STATE_BB, TUPLE_STATE_BBB, TUPLE_STATE_BBBB, TUPLE_STATE_W, TUPLE_STATE_WW, TUPLE_STATE_WWW, TUPLE_STATE_WWWW, TUPLE_STATE_BX_WX, TUPLE_STATE_BUTT
	};

	class TUPLE_INFO {
		int nScore;
		Point stPoints[];
		COLOR_TYPE eColor[];

		TUPLE_INFO() {
			stPoints = new Point[5];
			eColor = new COLOR_TYPE[5];

			for (int i = 0; i < 5; i++) {
				eColor[i] = COLOR_TYPE.COLOR_BUTT;
				stPoints[i] = new Point();
				stPoints[i].x = 0;
				stPoints[i].y = 0;
			}
		}
	};

	public GRID_POINT_INFO m_stGridPointInfo[][] = new GRID_POINT_INFO[LINE_NUM][LINE_NUM];
	public Point m_ptClickGridPoint = new Point();
	public Point m_ptComputerPoint = new Point();
	public Point m_ptLastComputerPoint = new Point();
	public boolean m_bIsWhite = false;
	public boolean m_bIsBoardClicked = false;
	public boolean m_bIsLastComputer = false;
	public boolean m_bIsGameOver = false;
	public TUPLE_INFO m_TupleInfo[] = new TUPLE_INFO[TUPLE_NUM];
	public int m_TupleScoreTable[] = new int[TUPLE_STATE.TUPLE_STATE_BUTT.ordinal()];
	public int m_GoSize = 0;  //棋子半径，即棋盘网格边长的一半
	public int m_HotSpotSize = 0; //触点区域大小，为棋子半径的一半
	public int m_MarkSize = 0;  //电脑走棋后+号的大小，为棋子半径的一半
	GoBangView MyGoBangView = null;
	public Button m_btnRestart = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyGoBangView = new GoBangView(this);
		m_btnRestart = new Button(this);
		m_btnRestart.setText(R.string.restart);
		
		setContentView(R.layout.activity_go_bang);

		LinearLayout layout = (LinearLayout)findViewById(R.id.container);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,  
                LayoutParams.WRAP_CONTENT);   
     
        btn_params.gravity = Gravity.CENTER_HORIZONTAL;  
        m_btnRestart.setLayoutParams(btn_params);
        
		layout.addView(m_btnRestart);
        layout.addView(MyGoBangView);
        
        m_btnRestart.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				int i, j;
				
				m_ptComputerPoint.x = 0;
				m_ptComputerPoint.y = 0;
				m_ptClickGridPoint.x = 0;
				m_ptClickGridPoint.y = 0;
				m_ptLastComputerPoint.x = 0;
				m_ptLastComputerPoint.y = 0;
				
				m_bIsWhite = false;
				m_bIsBoardClicked = false;
				m_bIsLastComputer = false;
				m_bIsGameOver = false;
				
				for (i = 0; i < TUPLE_NUM; i++) {			
					for (j = 0; j < 5; j++) {		
						m_TupleInfo[i].eColor[j] = COLOR_TYPE.COLOR_BUTT;
						m_TupleInfo[i].stPoints[j].x = -1;
						m_TupleInfo[i].stPoints[j].y = -1;
					}
				}
				
				for (i = 0; i < LINE_NUM; i++) {
					for (j = 0; j < LINE_NUM; j++) {
						m_stGridPointInfo[i][j].IsHasGoBang = false;
						m_stGridPointInfo[i][j].eColorType = COLOR_TYPE.COLOR_BUTT;
						m_stGridPointInfo[i][j].ptGridPoint.x = -1;
						m_stGridPointInfo[i][j].ptGridPoint.y = -1;
						m_stGridPointInfo[i][j].nScore = -1;
					}
				}
				
				MyGoBangView.invalidate();
				MyGoBangView.forceLayout();
				MyGoBangView.requestLayout();
			}
        	
        });
		
		System.out.println("OnCreate!");
		Log.i(getPackageName(), "OnCreate!");
		
		int i, j;

		for (i = 0; i < LINE_NUM; i++) {
			for (j = 0; j < LINE_NUM; j++) {
				m_stGridPointInfo[i][j] = new GRID_POINT_INFO();
			}
		}
		
		for (i = 0; i < TUPLE_NUM; i++) {
			m_TupleInfo[i] = new TUPLE_INFO();
		}
		
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BLANK.ordinal()] = 7;
		
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_B.ordinal()] = 35;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BB.ordinal()] = 800;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BBB.ordinal()] = 15000;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BBBB.ordinal()] = 800000;
		
		/*
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_W.ordinal()] = 15;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WW.ordinal()] = 400;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WWW.ordinal()] = 1800;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WWWW.ordinal()] = 100000;
		*/
		
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_W.ordinal()] = 35;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WW.ordinal()] = 800;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WWW.ordinal()] = 15000;
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WWWW.ordinal()] = 800000;
		
		
		m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BX_WX.ordinal()] = 0;
	}

	public class GoBangView extends View {

		private Paint mPaint;
		private Point mPoint;
		private Bitmap mBitmap; // 保存绘制的底图
		private Canvas mCanvas;
		private Point mBestPoint;

		GoBangView(Context context) {
			super(context);

			mPaint = new Paint();
			mPoint = new Point();
			mBestPoint = new Point();

			setOnTouchListener(new TouchViewListener());
			setBackgroundColor(Color.GRAY);
		}
		
		GoBangView(Context context, AttributeSet attrs)
		{
			super(context);
		}

		GoBangView(Context context, AttributeSet attrs, int defStyle)
		{
			super(context);
		}
		
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			mBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Bitmap.Config.RGB_565);
			mCanvas = new Canvas(mBitmap);

			Rect rect = new Rect(0, 0, getWidth(), getHeight());

			// 计算格子尺寸
			int nInterval = (getWidth() > getHeight() ? getHeight()
					: getWidth() - 2 * MARGIN_SIZE) / GRID_NUM;
			
			m_GoSize = nInterval / 2;
			m_HotSpotSize = m_GoSize / 2;
			m_MarkSize = m_GoSize / 2;

			mPoint.x = rect.centerX();
			mPoint.y = rect.centerY();

			int i, j, nLeftX, nLeftY, nRightX, nRightY;

			int nCirclePointX, nCirclePointY;

			nLeftX = mPoint.x - GRID_NUM * nInterval / 2;
			nLeftY = mPoint.y - GRID_NUM * nInterval / 2;
			nRightX = mPoint.x + GRID_NUM * nInterval / 2;
			nRightY = mPoint.y + GRID_NUM * nInterval / 2;

			mPaint.setColor(Color.BLACK);
			mPaint.setStyle(Style.FILL);
			mPaint.setStrokeWidth(2);

			// 画15条水平线
			for (i = 0; i < LINE_NUM; i++) {
				canvas.drawLine(nLeftX, nLeftY + i * nInterval, nRightX, nLeftY
						+ i * nInterval, mPaint);

				// 每水平条线上各点的y坐标相同
				for (j = 0; j < LINE_NUM; j++) {
					m_stGridPointInfo[i][j].ptGridPoint.y = nLeftY + i
							* nInterval;
				}
			}

			// 画15条竖直线
			for (j = 0; j < LINE_NUM; j++) {
				canvas.drawLine(nLeftX + j * nInterval, nLeftY, nLeftX + j
						* nInterval, nRightY, mPaint);

				// 每条竖直线上各点的x坐标相同
				for (i = 0; i < LINE_NUM; i++) {
					m_stGridPointInfo[i][j].ptGridPoint.x = nLeftX + j
							* nInterval;
				}
			}
			
			for (i = 0; i < LINE_NUM; i++) {
				for (j = 0; j < LINE_NUM; j++) {
					if (true == m_stGridPointInfo[i][j].IsHasGoBang) {
						if (COLOR_TYPE.COLOR_WHITE.ordinal() == m_stGridPointInfo[i][j].eColorType
								.ordinal()) {
							mPaint.setColor(Color.WHITE);
						} else {
							mPaint.setColor(Color.BLACK);
						}

						nCirclePointX = m_stGridPointInfo[i][j].ptGridPoint.x;
						nCirclePointY = m_stGridPointInfo[i][j].ptGridPoint.y;

						canvas.drawCircle(nCirclePointX, nCirclePointY,
								m_GoSize, mPaint);

						if (m_stGridPointInfo[i][j].ptGridPoint.x == m_ptComputerPoint.x
								&& m_stGridPointInfo[i][j].ptGridPoint.y == m_ptComputerPoint.y) {
							/* 画+号 */
							mPaint.setColor(Color.RED);
							canvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y, 
									m_ptComputerPoint.x - m_MarkSize, m_ptComputerPoint.y,
									mPaint);

							canvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y, 
									m_ptComputerPoint.x + m_MarkSize, m_ptComputerPoint.y,
									mPaint);

							canvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y,
									m_ptComputerPoint.x, m_ptComputerPoint.y - m_MarkSize, mPaint);

							canvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y, 
									m_ptComputerPoint.x, m_ptComputerPoint.y + m_MarkSize, mPaint);
						}
					}
				}
			}

		}

		public void DrawHumanGoBang() {
			mPaint.setStyle(Style.FILL);

			System.out.println("Human");

			
			if (m_bIsWhite) {
				mPaint.setColor(Color.WHITE);

			} else {
				mPaint.setColor(Color.BLACK);
			}

			mCanvas.drawCircle(m_ptClickGridPoint.x, m_ptClickGridPoint.y,
					m_GoSize, mPaint);
		}

		void DrawComputerGoBang() {

			System.out.println("Computer");
			
			if (m_bIsWhite) {
				mPaint.setColor(Color.WHITE);
			} else {
				mPaint.setColor(Color.BLACK);
			}

			mCanvas.drawCircle(m_ptComputerPoint.x, m_ptComputerPoint.y,
					m_GoSize, mPaint);

			/* 画+号 */
			mPaint.setColor(Color.RED);
			mCanvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y,
					m_ptComputerPoint.x - m_MarkSize, m_ptComputerPoint.y,
					mPaint);

			mCanvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y,
					m_ptComputerPoint.x + m_MarkSize, m_ptComputerPoint.y,
					mPaint);

			mCanvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y
					- m_MarkSize, m_ptComputerPoint.x, m_ptComputerPoint.y
					- m_MarkSize, mPaint);

			mCanvas.drawLine(m_ptComputerPoint.x, m_ptComputerPoint.y,
					m_ptComputerPoint.x, m_ptComputerPoint.y + m_MarkSize,
					mPaint);

			/* 清除上一步的+号 */
			if ((0 != m_ptLastComputerPoint.x)
					&& (0 != m_ptLastComputerPoint.y)) {

				if (m_bIsWhite) {
					mPaint.setColor(Color.WHITE);
				} else {
					mPaint.setColor(Color.BLACK);
				}

				mCanvas.drawCircle(m_ptLastComputerPoint.x,
						m_ptLastComputerPoint.y, m_GoSize, mPaint);
			}

			m_ptLastComputerPoint = m_ptComputerPoint;
		}

		boolean IsGameOver() {
			int i;

			for (i = 0; i < TUPLE_NUM; i++) {
				if (m_TupleInfo[i].eColor[0].ordinal() != COLOR_TYPE.COLOR_BUTT.ordinal()
						&& m_TupleInfo[i].eColor[0].ordinal() == m_TupleInfo[i].eColor[1].ordinal()
						&& m_TupleInfo[i].eColor[0].ordinal() == m_TupleInfo[i].eColor[2].ordinal()
						&& m_TupleInfo[i].eColor[0].ordinal() == m_TupleInfo[i].eColor[3].ordinal()
						&& m_TupleInfo[i].eColor[0].ordinal() == m_TupleInfo[i].eColor[4].ordinal()) {
					m_bIsGameOver = true;
					return true;
				}
			}

			return false;
		}

		int GetTupleScore(int nWhiteCnt, int nBlackCnt) {
			if (5 == nWhiteCnt || 5 == nBlackCnt) {
				return -1;
			}

			if (nWhiteCnt < 0 || nWhiteCnt > 4 || nBlackCnt < 0
					|| nBlackCnt > 4) {
				return -1;
			}

			if (nWhiteCnt == 0 && nBlackCnt == 0) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BLANK
						.ordinal()];
			} else if (nWhiteCnt == 0 && nBlackCnt == 1) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_B.ordinal()];
			} else if (nWhiteCnt == 0 && nBlackCnt == 2) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BB.ordinal()];
			} else if (nWhiteCnt == 0 && nBlackCnt == 3) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BBB.ordinal()];
			} else if (nWhiteCnt == 0 && nBlackCnt == 4) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BBBB.ordinal()];
			} else if (nWhiteCnt == 1 && nBlackCnt == 0) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_W.ordinal()];
			} else if (nWhiteCnt == 2 && nBlackCnt == 0) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WW.ordinal()];
			} else if (nWhiteCnt == 3 && nBlackCnt == 0) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WWW.ordinal()];
			} else if (nWhiteCnt == 4 && nBlackCnt == 0) {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_WWWW.ordinal()];
			} else {
				return m_TupleScoreTable[TUPLE_STATE.TUPLE_STATE_BX_WX
						.ordinal()];
			}
		}

		void UpdateTupleInfo() {
			int i, j, k, t, nWhiteCount, nBlackCount, nIndex;

			nIndex = 0;
			nWhiteCount = 0;
			nBlackCount = 0;

			/* 统计各行五元组信息 */
			for (i = 0; i < LINE_NUM; i++) {
				for (j = 0; j < LINE_NUM - 4; j++) {
					for (k = 0; k < 5; k++) {
						m_TupleInfo[nIndex].stPoints[k] = m_stGridPointInfo[i][j
								+ k].ptGridPoint;
						m_TupleInfo[nIndex].eColor[k] = m_stGridPointInfo[i][j
								+ k].eColorType;

						switch (m_stGridPointInfo[i][j + k].eColorType) {
						case COLOR_WHITE:
							nWhiteCount++;
							break;

						case COLOR_BLACK:
							nBlackCount++;
							break;
						default:
							break;
						}
					}

					m_TupleInfo[nIndex].nScore = GetTupleScore(nWhiteCount,
							nBlackCount);
					nIndex++; // 165
					nWhiteCount = 0;
					nBlackCount = 0;
				}
			}

			/* 统计各列五元组信息 */
			for (j = 0; j < LINE_NUM; j++) {
				for (i = 0; i < LINE_NUM - 4; i++) {
					for (k = 0; k < 5; k++) {
						m_TupleInfo[nIndex].stPoints[k] = m_stGridPointInfo[i
								+ k][j].ptGridPoint;
						m_TupleInfo[nIndex].eColor[k] = m_stGridPointInfo[i + k][j].eColorType;

						switch (m_stGridPointInfo[i + k][j].eColorType) {
						case COLOR_WHITE:
							nWhiteCount++;
							break;

						case COLOR_BLACK:
							nBlackCount++;
							break;
						default:
							break;
						}
					}

					m_TupleInfo[nIndex].nScore = GetTupleScore(nWhiteCount,
							nBlackCount);
					nIndex++; // 330
					nWhiteCount = 0;
					nBlackCount = 0;
				}
			}

			/* 统计左上到右下五元组信息 */
			/* 统计左上半部分五元组信息（含对角线） */
			for (i = 4; i < LINE_NUM; i++) {
				t = 0;
				for (j = 0; j <= (i - 4); j++) {
					for (k = 0; k < 5; k++) {
						m_TupleInfo[nIndex].stPoints[k] = m_stGridPointInfo[i
								- k - t][j + k].ptGridPoint;
						m_TupleInfo[nIndex].eColor[k] = m_stGridPointInfo[i - k
								- t][j + k].eColorType;

						switch (m_stGridPointInfo[i - k - t][j + k].eColorType) {
						case COLOR_WHITE:
							nWhiteCount++;
							break;

						case COLOR_BLACK:
							nBlackCount++;
							break;
						default:
							break;
						}
					}
					t++;

					m_TupleInfo[nIndex].nScore = GetTupleScore(nWhiteCount,
							nBlackCount);
					nIndex++; // 396
					nWhiteCount = 0;
					nBlackCount = 0;
				}
			}

			/* 统计右下半部分五元组信息 */
			for (i = 1; i < LINE_NUM - 4; i++) {
				t = 0;
				for (j = LINE_NUM - 1; j >= i + 4; j--) {
					for (k = 0; k < 5; k++) {
						m_TupleInfo[nIndex].stPoints[k] = m_stGridPointInfo[i
								+ k + t][j - k].ptGridPoint;
						m_TupleInfo[nIndex].eColor[k] = m_stGridPointInfo[i + k
								+ t][j - k].eColorType;

						switch (m_stGridPointInfo[i + k + t][j - k].eColorType) {
						case COLOR_WHITE:
							nWhiteCount++;
							break;

						case COLOR_BLACK:
							nBlackCount++;
							break;
						default:
							break;
						}
					}
					t++;

					m_TupleInfo[nIndex].nScore = GetTupleScore(nWhiteCount,
							nBlackCount);
					nIndex++; // 451
					nWhiteCount = 0;
					nBlackCount = 0;
				}
			}

			/* 统计左下到右上五元组信息 */
			/* 统计左下半部五元组信息（含对角线) */
			for (i = 0; i < LINE_NUM - 4; i++) {
				t = 0;
				for (j = 0; j < LINE_NUM - i - 4; j++) {
					for (k = 0; k < 5; k++) {
						m_TupleInfo[nIndex].stPoints[k] = m_stGridPointInfo[i
								+ k + t][j + k].ptGridPoint;
						m_TupleInfo[nIndex].eColor[k] = m_stGridPointInfo[i + k
								+ t][j + k].eColorType;

						switch (m_stGridPointInfo[i + k + t][j + k].eColorType) {
						case COLOR_WHITE:
							nWhiteCount++;
							break;

						case COLOR_BLACK:
							nBlackCount++;
							break;
						default:
							break;
						}
					}
					t++;

					m_TupleInfo[nIndex].nScore = GetTupleScore(nWhiteCount,
							nBlackCount);
					nIndex++; // 517
					nWhiteCount = 0;
					nBlackCount = 0;
				}
			}

			/* 统计右上半部五元组信息 */
			for (i = 4; i < LINE_NUM - 1; i++) {
				t = 0;
				for (j = LINE_NUM - 1; j >= LINE_NUM - i + 3; j--) {
					for (k = 0; k < 5; k++) {
						m_TupleInfo[nIndex].stPoints[k] = m_stGridPointInfo[i
								- k - t][j - k].ptGridPoint;
						m_TupleInfo[nIndex].eColor[k] = m_stGridPointInfo[i - k
								- t][j - k].eColorType;

						switch (m_stGridPointInfo[i - k - t][j - k].eColorType) {
						case COLOR_WHITE:
							nWhiteCount++;
							break;

						case COLOR_BLACK:
							nBlackCount++;
							break;
						default:
							break;
						}
					}
					t++;

					m_TupleInfo[nIndex].nScore = GetTupleScore(nWhiteCount,
							nBlackCount);
					nIndex++; // 572
					nWhiteCount = 0;
					nBlackCount = 0;
				}
			}
		}

		void GetBestPoint() {
			int i, j, k, t;
			int nBestScore = 0;

			/* 计算每个空白位所在五元组分值总和 */
			for (i = 0; i < LINE_NUM; i++) {
				for (j = 0; j < LINE_NUM; j++) {
					m_stGridPointInfo[i][j].nScore = 0;

					if (!m_stGridPointInfo[i][j].IsHasGoBang) {
						/* 遍历所有五元组，累加所有包含该空白位的五元组的分值 */
						for (k = 0; k < TUPLE_NUM; k++) {
							for (t = 0; t < 5; t++) {
								if (m_stGridPointInfo[i][j].ptGridPoint.x == m_TupleInfo[k].stPoints[t].x
										&& m_stGridPointInfo[i][j].ptGridPoint.y == m_TupleInfo[k].stPoints[t].y) {
									/* 累加该空白位所在的五元组的分值 */
									m_stGridPointInfo[i][j].nScore += m_TupleInfo[k].nScore;
									break;
								}
							}
						}

						if (nBestScore < m_stGridPointInfo[i][j].nScore) {
							nBestScore = m_stGridPointInfo[i][j].nScore;
							mBestPoint = m_stGridPointInfo[i][j].ptGridPoint;
						}
					}
				}
			}

			m_ptComputerPoint = mBestPoint;
		}

		class TouchViewListener implements OnTouchListener {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				int i, j, k, t;

				if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
					
					if (true == m_bIsGameOver) {
						System.out.println("Game over !");
						return true;
					}
					
					for (i = 0; i < LINE_NUM; i++) {
						for (j = 0; j < LINE_NUM; j++) {
							if (Math.abs(arg1.getX()
									- m_stGridPointInfo[i][j].ptGridPoint.x) < m_HotSpotSize
									&& Math.abs(arg1.getY()
											- m_stGridPointInfo[i][j].ptGridPoint.y) < m_HotSpotSize) {
								if (m_stGridPointInfo[i][j].IsHasGoBang) {
									return true;
								}

								m_ptClickGridPoint = m_stGridPointInfo[i][j].ptGridPoint;
								m_stGridPointInfo[i][j].IsHasGoBang = true;
								m_bIsBoardClicked = true;

								if (m_bIsWhite) {
									m_stGridPointInfo[i][j].eColorType = COLOR_TYPE.COLOR_WHITE;
								} else {
									m_stGridPointInfo[i][j].eColorType = COLOR_TYPE.COLOR_BLACK;
								}

								System.out.println("Human: [" + i + ", " + j + "]");
								
								DrawHumanGoBang(); // 人走棋
								m_bIsLastComputer = false;
								m_bIsWhite = !m_bIsWhite;
								m_bIsBoardClicked = false;

								UpdateTupleInfo();

								if (true == IsGameOver()) {			
									System.out.println("Game over ! Human Win !");
									invalidate();
									forceLayout();
									requestLayout();
									return true;
								}

								GetBestPoint();
								DrawComputerGoBang(); // 电脑走棋
								m_bIsLastComputer = true;

								/* 更新棋盘状态 */
								for (k = 0; k < LINE_NUM; k++) {
									for (t = 0; t < LINE_NUM; t++) {
										if (m_stGridPointInfo[k][t].ptGridPoint.x == m_ptComputerPoint.x
												&& m_stGridPointInfo[k][t].ptGridPoint.y == m_ptComputerPoint.y) {
											m_stGridPointInfo[k][t].IsHasGoBang = true;

											if (m_bIsWhite) {
												m_stGridPointInfo[k][t].eColorType = COLOR_TYPE.COLOR_WHITE;
											} else {
												m_stGridPointInfo[k][t].eColorType = COLOR_TYPE.COLOR_BLACK;
											}
										}
									}
								}

								m_bIsWhite = !m_bIsWhite;

								UpdateTupleInfo();

								if (true == IsGameOver()) {
									System.out.println("Game over ! Computer Win !");
									invalidate();
									forceLayout();
									requestLayout();
									return true;
								}
							}
						}
					}
				}
				
				invalidate();
				forceLayout();
				requestLayout();
				return true;
			}
		}
	}
}

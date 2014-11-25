package hanwha.meta;

import static hanwha.util.Extend.extend;
import static org.junit.Assert.assertEquals;
import hanwha.util.IntList;
import hanwha.util.IntSet;
import hanwha.util.PairSet;
import hanwha.util.Set;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 개인 보험 보험종류별 상품 판매 정보
 */
public class Meta {
	private static final String  DATA_DIR = ".";
	private static final File   DATA_FILE = new File(DATA_DIR,
	                                                 Meta.class.getName());
	private static boolean     refreshing = false;
	private static AtomicInteger useCount = new AtomicInteger();

	static {
		try {
			refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[]    nameSet; // 보험종류/상품 이름
	private static    int[]  goodCodes; // 상품 코드
	private static  short[]  goodNames; // 상품 이름 인덱스
	private static   byte[]  goodRates; // 실손회사부담율
	private static    int[]     intSet; // 시작일/종료일
	private static  short[][]  pairSet; // (시작일 인덱스, 종료일 인덱스) 짝
	private static  short[]   goodList; // 상품 목록
	private static  short[]  datesList; // 시작일/종료일 목록
	private static    int[]  irkdCodes; // 보험종류 코드
	private static  short[][] irkdData; // (보종명,상품 목록,판매기간 목록,상품수)

	/**
	 * 데이터를 갱신한다.
	 * 데이터 파일이 없으면 데이터베이스에서 다운로드하여 새로 만든다.
	 * @throws Exception 데이터 읽기 에러 또는 download() 에러.
	 */
	public static synchronized void refresh() throws Exception {
		final String DATA_PATH = DATA_FILE.getCanonicalPath();
		time(null);
		if (DATA_FILE.length() < 1) {
			download(DATA_FILE);
			if (DATA_FILE.length() < 1) {
				throw new Exception(DATA_PATH + " 파일을 만들 수 없습니다.");
			}
		}
        try (ObjectInputStream in = new ObjectInputStream(
                                    new BufferedInputStream(
                                    new FileInputStream(DATA_FILE)))) {
    		time(DATA_PATH);

    		refreshing = true;
    		while (0 < useCount.get()) {
    			Thread.sleep(1);
    		}
    		nameSet    =  (String[]) in.readObject();
    		goodCodes  =     (int[]) in.readObject();
    		goodNames  =   (short[]) in.readObject();
    		goodRates  =    (byte[]) in.readObject();
    		intSet     =     (int[]) in.readObject();
    		pairSet    = (short[][]) in.readObject();
    		goodList   =   (short[]) in.readObject();
    		datesList  =   (short[]) in.readObject();
    		irkdCodes  =     (int[]) in.readObject();
    		irkdData   = (short[][]) in.readObject();
    		refreshing = false;
    		time("Read");
        }
		System.out.format("%d KBytes\n", (DATA_FILE.length() + 1023)/1024);
	}

	private static void enter() {
		useCount.incrementAndGet();
		if (refreshing) {
			useCount.decrementAndGet();
			while (refreshing) {
				try { Thread.sleep(1); } catch (Exception e) {}
			}
			useCount.incrementAndGet();
		}
	}

	private static void exit() {
		useCount.decrementAndGet();
	}

	private static int today() {
		Calendar now = Calendar.getInstance();
		return   now.get(Calendar.YEAR)       * 10000 +
		        (now.get(Calendar.MONTH) + 1) * 100 +
		         now.get(Calendar.DATE);
	}

	/**
	 * SQL을 실행하여 얻은 데이터로 메모리 이미지를 만들고 파일에 기록한다.
	 * @param file 메모리 이미지 파일
	 * @throws Exception 테이블 읽기 오류, 데이터 배열 원소 크기 오류.
	 */
	public static void download(File file) throws Exception {
		Set<String> nameSet = new Set<>(1000);
		Set<GoodIn> goodSet = new Set<>(1000);
		IntSet       intSet = new IntSet(100);
		PairSet     pairSet = new PairSet(100);
		IntList    goodList = new IntList(20000, 1000);
		IntList   datesList = new IntList(3000, 500);
		IrkdSet     irkdSet = new IrkdSet(1000);

		time(null);
		List<Row> rows = Dao.getRowList();    // 데이터를 모두 읽는다
		time("SQL");

		int rowCount = rows.size();           // 데이터 줄 수
		rows.add(new Row());                  // 데이터의 끝을 표시한다

		for (int i = 0; i < rowCount; i++) {
			Row row = rows.get(i);
			goodList.append(goodSet.find(new GoodIn(row.goodCode,
			                                        nameSet.find(row.goodName),
			                                        row.goodRate)));
            datesList.append(pairSet.find(intSet.find(row.strtDate),
                                          intSet.find(row.endDate)));
			if (row.irkdCode != rows.get(i + 1).irkdCode) {
				irkdSet.append(row.irkdCode, nameSet.find(row.irkdName),
				               goodList.find(), datesList.find());
			}
		}
		int     goodCount = goodSet.size();
		int[]   goodCodes = new   int[goodCount];
		short[] goodNames = new short[goodCount];
		byte[]  goodRates = new  byte[goodCount];
		for (int i = 0; i < goodCount; i++) {
			GoodIn  good = goodSet.get(i);
			goodCodes[i] = good.code;
			goodNames[i] = good.name;
			goodRates[i] = good.rate;
		}
		time("Index");

		try (ObjectOutputStream out = new ObjectOutputStream(
        		                      new BufferedOutputStream(
        		                      new FileOutputStream(file)))) {
        	out.writeObject(nameSet.copy());
        	out.writeObject(goodCodes);
        	out.writeObject(goodNames);
        	out.writeObject(goodRates);
        	out.writeObject(intSet.copy());
        	out.writeObject(pairSet.copyToShorts());
        	out.writeObject(goodList.copyToShorts());
        	out.writeObject(datesList.copyToShorts());
        	out.writeObject(irkdSet.copyCodes());
        	out.writeObject(irkdSet.copyData());
        }
		time("Write");

		System.out.format("\n%d rows --> %d KBytes (%s)\n" +
		     "irkdSet[%d] goodSet[%d] nameSet[%d] intSet[%d] pairSet[%s]\n" +
		     "goodList[%d](%d) datesList[%d](%d)\n",
		      rowCount, (file.length() + 1023)/1024, file.getCanonicalPath(),
		       irkdSet.size(),   goodSet.size(),  nameSet.size(),
		        intSet.size(),   pairSet.size(),
		      goodList.size(),  goodList.indexSize(),
		     datesList.size(), datesList.indexSize());
	}

	/**
	 * 경과 시간을 출력한다.
	 * @param title 제목. null이면 경과 시간 측정 시작.
	 */
	private static void time(String title) {
		nano = System.nanoTime();
		if (title != null) {
			System.out.format("%s(%.3f초) ", title, (nano - nano0)/1000000000.);
		}
		nano0 = nano;
	}

	private static long nano0, nano;  // 나노 초 단위 경과 구간: 시작, 끝

	/**
	 * 기준일에 판매 중인 i-번째 보험종류 정보를 구한다.
	 * @param i 보험종류 인덱스
	 * @param 기준일  8자리 정수 (년 4자리, 월 2자리, 일 2자리)
	 * @return 보험종류 정보, 판매 중인 것이 없으면 null.
	 */
	private static Irkd getIrkd(int i, int 기준일) {
		List<Good>   goods = new ArrayList<>();
		int  goodListIndex = irkdData[1][i];
		int  termListIndex = irkdData[2][i];
		for (int goodCount = irkdData[3][i]; 0 < goodCount; goodCount--) {
			int g =  goodList[goodListIndex++];
			int t = datesList[termListIndex++];
			int strtDate = intSet[pairSet[0][t]];
			int  endDate = intSet[pairSet[1][t]];
			if (strtDate <= 기준일 && 기준일 <= endDate) {
				goods.add(new Good(goodCodes[g],
				                   nameSet[goodNames[g]],
				                   goodRates[g]));
			}
		}
		if (0 < goods.size()) {
			return new Irkd(irkdCodes[i], nameSet[irkdData[0][i]], goods);
		}
		return null;
	}

	/**
	 * 기준일에 판매 중인 한 보험종류 정보를 구한다.
	 * @param  보종코드_세  보험종류 코드 세
	 * @param  보종코드_목  보험종류 코드 목
	 * @param  기준일  8자리 정수 (년 4자리, 월 2자리, 일 2자리)
	 * @return 보험종류 정보, 판매 중인 것이 없으면 null.
	 */
	public static Irkd getIrkd(String 보종코드_세, String 보종코드_목, int 기준일) {
		int irkdCode = Integer.parseInt(보종코드_세 + 보종코드_목);
		enter();
		int     i = Arrays.binarySearch(irkdCodes, irkdCode);
		Irkd irkd = i < 0? null: getIrkd(i, 기준일);
		exit();
		return irkd;
	}

	/**
	 * 오늘 판매 중인 한 보험종류 정보를 구한다.
	 * @param  보종코드_세  보험종류 코드 세
	 * @param  보종코드_목  보험종류 코드 목
	 * @return 보험종류 정보, 판매 중인 것이 없으면 null.
	 */
	public static Irkd getIrkd(String 보종코드_세, String 보종코드_목) {
		return getIrkd(보종코드_세, 보종코드_목, today());
	}

	/**
	 * 기준일에 판매 중인 한 대분류 보험종류 정보를 구한다.
	 * @param  보종코드_세  보험종류 코드 세
	 * @param  기준일   8자리 정수 (년 4자리, 월 2자리, 일 2자리)
	 * @return 대분류에 들어가는 보험종류 정보 리스트
	 */
	public static List<Irkd> getIrkdList(String 보종코드_세, int 기준일) {
		int        irkdDtal = Integer.parseInt(보종코드_세);
		List<Irkd> irkdList = new ArrayList<>();
		enter();
		int i = -Arrays.binarySearch(irkdCodes, irkdDtal * 1000) - 1;
		for (; i < irkdCodes.length && irkdCodes[i] / 1000 == irkdDtal; i++) {
			Irkd irkd = getIrkd(i, 기준일);
			if (irkd != null) {
				irkdList.add(irkd);
			}
		}
		exit();
		return irkdList;
	}

	/**
	 * 오늘 판매 중인 한 대분류 보험종류 정보를 구한다.
	 * @param  보종코드_세  보험종류 코드 세
	 * @return 대분류에 들어가는 보험종류 정보 리스트
	 */
	public static List<Irkd> getIrkdList(String 보종코드_세) {
		return getIrkdList(보종코드_세, today());
	}

	/**
	 * 기준일에 판매 중인 모든 보험종류 정보를 구한다.
	 * @param  기준일  8자리 정수 (년 4자리, 월 2자리, 일 2자리)
	 * @return 대분류별 보험종류 정보 리스트의 리스트
	 */
	public static List<List<Irkd>> getIrkdListList(int 기준일) {
		List<List<Irkd>> irkdListList = new ArrayList<>();
		enter();
		int        irkdDtal = irkdCodes[0] / 1000;
		List<Irkd> irkdList = new ArrayList<>();
		for (int i = 0;;) {
			Irkd irkd = getIrkd(i, 기준일);
			if (irkd != null) {
				irkdList.add(irkd);
			}
			if (++i == irkdCodes.length || irkdDtal != irkdCodes[i] / 1000) {
				if (0 < irkdList.size()) {
					irkdListList.add(irkdList);
					irkdList = new ArrayList<>();
				}
				if (i == irkdCodes.length) {
					break;
				}
				irkdDtal = irkdCodes[i] / 1000;
			}
		}
		exit();
		return irkdListList;
	}

	/**
	 * 오늘 판매 중인 모든 보험종류 정보를 구한다.
	 * @return 대분류별 보험종류 정보 리스트의 리스트
	 */
	public static List<List<Irkd>> getIrkdListList() {
		return getIrkdListList(today());
	}

	/**
	 * 데이터를 출력하거나, 테이블에서 읽은 행과 맞추어 검사한다.
	 * @param from  출력 시작 보험종류 인덱스  -- 0, 1, 2, ...
	 * @param count 출력 보험종류 수 -- 0이면 데이터를 모두 검사한다.
	 * @param file  출력 파일 이름 -- null이거나 잘못된 이름이면 표준 출력으로 출력한다.
	 * @throws Exception 데이터베이스 테이블 읽기 오류.
	 */
	public static void print(int from, int count, String file)
		              throws Exception {
		boolean testing = count == 0;
		int          to = Math.min(from + count, irkdCodes.length);
		PrintStream out = System.out;
		List<Row>  rows = null;

		if (testing) {
			from = 0;                 // 처음부터
			to   = irkdCodes.length;  // 끝까지
			rows = Dao.getRowList();  // 테이블에서 읽은 데이터 리스트
		} else {
			if (file != null) {
				try {
					out = new PrintStream(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		int r = 0;                              // 행 인덱스
		for (int i = from; i < to; i++) {       // 보험종류 인덱스
			int       irkdCode = irkdCodes[i];
			String    irkdName = nameSet[irkdData[0][i]];
			int  goodListIndex = irkdData[1][i];
			int  termListIndex = irkdData[2][i];
			for (int goodCount = irkdData[3][i]; 0 < goodCount; goodCount--) {
				int g = goodList[goodListIndex++];
				int t = datesList[termListIndex++];
				int    goodCode = goodCodes[g];
				String goodName = nameSet[goodNames[g]];
				int    goodRate = goodRates[g];
				int    strtDate = intSet[pairSet[0][t]];
				int     endDate = intSet[pairSet[1][t]];
				if (testing) {
					String msg = "행 인덱스 = " + r;
					Row    row = rows.get(r++);
					assertEquals(msg, row.irkdCode, irkdCode);
					assertEquals(msg, row.irkdName, irkdName);
					assertEquals(msg, row.goodCode, goodCode);
					assertEquals(msg, row.goodName, goodName);
					assertEquals(msg, row.goodRate, goodRate);
					assertEquals(msg, row.strtDate, strtDate);
					assertEquals(msg, row.endDate,   endDate);
				} else {
					out.format("%d %d %3d %d %d %s %s\n",
					           irkdCode, goodCode, goodRate, strtDate, endDate,
					           irkdName, goodName);
				}
			}
		}
		if (testing) {
			assertEquals("행 수", rows.size(), r);  // 행 수가 맞는지 검사한다
		} else {
			if (out != System.out) {
				out.close();
			}
		}
	}

	/**
	 * 데이터를 콘솔에 출력한다.
	 * @param from  출력 시작 보험종류 인덱스  -- 0, 1, 2, ...
	 * @param count 출력 보험종류 수
	 * @throws Exception 데이터베이스 테이블 읽기 오류.
	 */
	public static void print(int from, int count) throws Exception {
		print(from, count, null);
	}

	/**
	 * 데이터를 테이블에서 읽은 행과 맞추어 검사한다.
	 * @throws Exception 데이터베이스 테이블 읽기 오류.
	 */
	public static void test() throws Exception {
		print(0, 0, null);
	}

	private Meta() {}
}

class GoodIn implements Comparable<GoodIn> {
	int   code;  // 상품 코드
	short name;  // 상품 이름 인덱스
	byte  rate;  // 실손회사부담율

	GoodIn(int code, int name, byte rate) {
		this.code = code;
		this.name = (short) name;
		this.rate = rate;
	}

	@Override
	public int compareTo(GoodIn o) {
		return code - o.code;
	}
}

class IrkdSet {
	private int[]     codes; // 보험종류 코드
	private short[][] data;  // (보험종류 이름, 상품 이름 목록, 판매기간 목록, 상품 수)
	private int       size;  // 보험종류 수

	/**
	 * @param initialCapacity 처음 배열 용량(= 보험종류 수 최대값)
	 */
	IrkdSet(int initialCapacity) {
		codes = new      int[initialCapacity];
		data  = new short[4][initialCapacity];
	}

	/**
	 * 보험종류 하나의 데이터를 배열에 넣는다.
	 * @param code  보험종류 코드
	 * @param name  보험종류 이름
	 * @param goods 상품 이름 목록 시작, 끝 인덱스
	 * @param dates 상품 판매기간 목록 시작, 끝 인덱스
	 */
	void append(int code, int name, int[] goods, int[] dates) {
		if (codes.length <= size) {
			codes = Arrays.copyOf(codes, extend(size));
			data  = copyData(extend(size));
		}
		codes[size]   = code;              // 보험종류 코드
		data[0][size] = (short) name;      // 보험종류 이름
		data[1][size] = (short) goods[0];  // 상품 이름 목록 시작 인덱스
		data[2][size] = (short) dates[0];  // 상품 판매기간 목록 시작 인덱스
		data[3][size] = (short) (goods[1] - goods[0]);  // 상품 수
		size++;
	}

	int size() {
		return size;
	}

	int[] copyCodes() {
		return Arrays.copyOf(codes, size);
	}

	short[][] copyData() {
		return copyData(size);
	}

	private short[][] copyData(int newSize) {
		return new short[][] {
			Arrays.copyOf(data[0], newSize),
			Arrays.copyOf(data[1], newSize),
			Arrays.copyOf(data[2], newSize),
			Arrays.copyOf(data[3], newSize)
		};
	}
}

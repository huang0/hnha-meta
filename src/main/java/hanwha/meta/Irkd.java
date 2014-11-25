package hanwha.meta;

import java.util.List;

/**
 * 보험종류
 */
public class Irkd {
	private String irkdCodeDtal;  // 보험종류 코드 세
	private String irkdCodeItem;  // 보험종류 코드 목
	private String inkdBsnsName;  // 보험종류 업무명
	private List<Good> goodList;  // 상품 목록

	Irkd(int irkdCode, String inkdBsnsName, List<Good> goodList) {
		this.irkdCodeDtal = String.valueOf(       irkdCode / 1000);
		this.irkdCodeItem = String.format("%03d", irkdCode % 1000);
		this.inkdBsnsName = inkdBsnsName;
		this.goodList     = goodList;
	}

	/**
	 * 상품 목록
	 * @return 상품 목록
	 */
	public List<Good> getGoodList() {
		return goodList;
	}

	/**
	 * 보험종류 코드 세
	 * @return 보험종류 코드 세
	 */
	public String getIrkdCodeDtal() {
		return irkdCodeDtal;
	}

	/**
	 * 보험종류 코드 목
	 * @return 보험종류 코드 목
	 */
	public String getIrkdCodeItem() {
		return irkdCodeItem;
	}

	/**
	 * 보험종류 업무명
	 * @return 보험종류 업무명
	 */
	public String getInkdBsnsName() {
		return inkdBsnsName;
	}
}

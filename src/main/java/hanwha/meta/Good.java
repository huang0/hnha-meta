package hanwha.meta;

/**
 * 상품
 */
public class Good {
	private String goodCodeDtal;  // 상품 코드 세
	private String goodCodeItem;  // 상품 코드 목
	private String goodBsnsName;  // 상품 업무명
	private int    pmmiCmpnCgrt;  // 실손 회사부담율

	Good(int goodCode, String inkdBsnsName, int pmmiCmpnCgrt) {
		this.goodCodeDtal = String.valueOf(       goodCode / 1000);
		this.goodCodeItem = String.format("%03d", goodCode % 1000);
		this.goodBsnsName = inkdBsnsName;
		this.pmmiCmpnCgrt = pmmiCmpnCgrt;
	}

	/**
	 * 상품 코드 세
	 * @return 상품 코드 세
	 */
	public String getGoodCodeDtal() {
		return goodCodeDtal;
	}

	/**
	 * 상품 코드 목
	 * @return 상품 코드 목
	 */
	public String getGoodCodeItem() {
		return goodCodeItem;
	}

	/**
	 * 상품 업무명
	 * @return 상품 업무명
	 */
	public String getGoodBsnsName() {
		return goodBsnsName;
	}

	/**
	 * 실손 회사부담율
	 * @return 실손 회사부담율
	 */
	public int getPmmiCmpnCgrt() {
		return pmmiCmpnCgrt;
	}
}

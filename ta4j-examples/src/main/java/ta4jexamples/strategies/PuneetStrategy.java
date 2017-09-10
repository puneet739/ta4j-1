package ta4jexamples.strategies;

import eu.verdelhan.ta4j.BaseStrategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.CCIIndicator;
import eu.verdelhan.ta4j.indicators.SMAIndicator;
import eu.verdelhan.ta4j.indicators.bollinger.BollingerBandDifference;
import eu.verdelhan.ta4j.indicators.bollinger.BollingerBandsMiddleIndicator;
import eu.verdelhan.ta4j.indicators.bollinger.BollingerBandsUpperIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.statistics.StandardDeviationIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class PuneetStrategy {
	
	public static Strategy buildStrategy(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}
		
		ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
		SMAIndicator sma = new SMAIndicator(closePriceIndicator, 20);
		
		StandardDeviationIndicator sd20 = new StandardDeviationIndicator(closePriceIndicator, 20);
		BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);
		BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand, sd20);

		BollingerBandDifference bandDifference= new BollingerBandDifference(upperBand, middleBand);
		SMAIndicator smaBandDifference = new SMAIndicator(bandDifference, 30);

		
		CCIIndicator longCci = new CCIIndicator(series, 200);
		CCIIndicator shortCci = new CCIIndicator(series, 5);
		Decimal plus100 = Decimal.HUNDRED;
		Decimal minus100 = Decimal.valueOf(-100);

		UnderIndicatorRule underIndicator = new UnderIndicatorRule(bandDifference, Decimal.valueOf("2.0"));
		
		Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
				.and(new UnderIndicatorRule(shortCci, minus100)); // Signal

		for (int i=series.getTickCount(); i>0; i--){
			underIndicator.isSatisfied(i);	
		}
		
		Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
				.and(new OverIndicatorRule(shortCci, plus100)); // Signal

		Strategy strategy = new BaseStrategy(entryRule, exitRule);
		strategy.setUnstablePeriod(5);
		return strategy;
	}
	
	
}

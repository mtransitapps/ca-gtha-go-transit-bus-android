package org.mtransit.parser.ca_gtha_go_transit_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://www.gotransit.com/en/information-resources/software-developers
// https://www.gotransit.com/fr/ressources-informatives/dveloppeurs-de-logiciel
public class GTHAGOTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new GTHAGOTransitBusAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN_FR;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public @Nullable String getRouteIdCleanupRegex() {
		return "^\\d+-";
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR = "387C2B"; // GREEN (AGENCY WEB SITE CSS)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final int rsn = Integer.parseInt(gRoute.getRouteShortName());
		switch (rsn) {
		// @formatter:off
		case 11: return "98002e"; // St. Catharines / Niagara on the Lake
		case 70: return "794500"; //
		// @formatter:on
		}
		throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_LETTER = Pattern.compile("(^([A-Z]) )", Pattern.CASE_INSENSITIVE);

	private static final Pattern SPECIAL_ = Pattern.compile("(^special$)", Pattern.CASE_INSENSITIVE);

	@Nullable
	@Override
	public String selectDirectionHeadSign(@Nullable String headSign1, @Nullable String headSign2) {
		if (StringUtils.equals(headSign1, headSign2)) {
			return null; // can NOT select
		}
		final boolean headSign1StartsWithLetter = headSign1 != null && STARTS_WITH_LETTER.matcher(headSign1).matches();
		final boolean headSign2StartsWithLetter = headSign2 != null && STARTS_WITH_LETTER.matcher(headSign2).matches();
		if (headSign1StartsWithLetter) {
			if (!headSign2StartsWithLetter) {
				return headSign2;
			}
		} else if (headSign2StartsWithLetter) {
			return headSign1;
		}
		final boolean headSign1Special = headSign1 != null && SPECIAL_.matcher(headSign1).matches();
		final boolean headSign2Special = headSign2 != null && SPECIAL_.matcher(headSign2).matches();
		if (headSign1Special) {
			if (!headSign2Special) {
				return headSign2;
			}
		} else if (headSign2Special) {
			return headSign1;
		}
		return null;
	}

	@NotNull
	@Override
	public String cleanDirectionHeadsign(int directionId, boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = STARTS_WITH_RSN.matcher(directionHeadSign).replaceAll(EMPTY);
		directionHeadSign = SPECIAL_.matcher(directionHeadSign).replaceAll(EMPTY);
		return super.cleanDirectionHeadsign(directionId, fromStopName, directionHeadSign);
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^\\d{2,3}([A-Z]?)(\\s+)- )", Pattern.CASE_INSENSITIVE);
	private static final String STARTS_WITH_RSN_REPLACEMENT = "$2$3";

	private static final Pattern CLEAN_DASH = Pattern.compile("(^\\s*-\\s*|\\s*-\\s*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern BUS_TERMINAL = Pattern.compile("( bus loop| bus terminal| bus term[.]?| terminal| term[.]?)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(getFirstLanguageNN(), tripHeadsign, getIgnoredWords());
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(STARTS_WITH_RSN_REPLACEMENT);
		tripHeadsign = BUS_TERMINAL.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CLEAN_DASH.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(getFirstLanguageNN(), tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"GO",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(getFirstLanguageNN(), gStopName);
	}

	private static final String SID_UN = "UN";
	private static final int UN_SID = 9021;
	private static final String SID_EX = "EX";
	private static final int EX_SID = 9022;
	private static final String SID_MI = "MI";
	private static final int MI_SID = 9031;
	private static final String SID_LO = "LO";
	private static final int LO_SID = 9033;
	private static final String SID_DA = "DA";
	private static final int DA_SID = 9061;
	private static final String SID_SC = "SC";
	private static final int SC_SID = 9062;
	private static final String SID_EG = "EG";
	private static final int EG_SID = 9063;
	private static final String SID_GU = "GU";
	private static final int GU_SID = 9081;
	private static final String SID_RO = "RO";
	private static final int RO_SID = 9091;
	private static final String SID_PO = "PO";
	private static final int PO_SID = 9111;
	private static final String SID_CL = "CL";
	private static final int CL_SID = 9121;
	private static final String SID_OA = "OA";
	private static final int OA_SID = 9131;
	private static final String SID_BO = "BO";
	private static final int BO_SID = 9141;
	private static final String SID_AP = "AP";
	private static final int AP_SID = 9151;
	private static final String SID_BU = "BU";
	private static final int BU_SID = 9161;
	private static final String SID_AL = "AL";
	private static final int AL_SID = 9171;
	private static final String SID_PIN = "PIN";
	private static final int PIN_SID = 9911;
	private static final String SID_AJ = "AJ";
	private static final int AJ_SID = 9921;
	private static final String SID_WH = "WH";
	private static final int WH_SID = 9939;
	private static final String SID_OS = "OS";
	private static final int OS_SID = 9941;
	private static final String SID_BL = "BL";
	private static final int BL_SID = 9023;
	private static final String SID_KP = "KP";
	private static final int KP_SID = 9032;
	private static final String SID_WE = "WE";
	private static final int WE_SID = 9041;
	private static final String SID_ET = "ET";
	private static final int ET_SID = 9042;
	private static final String SID_OR = "OR";
	private static final int OR_SID = 9051;
	private static final String SID_OL = "OL";
	private static final int OL_SID = 9052;
	private static final String SID_AG = "AG";
	private static final int AG_SID = 9071;
	private static final String SID_DI = "DI";
	private static final int DI_SID = 9113;
	private static final String SID_CO = "CO";
	private static final int CO_SID = 9114;
	private static final String SID_ER = "ER";
	private static final int ER_SID = 9123;
	private static final String SID_HA = "HA";
	private static final int HA_SID = 9181;
	private static final String SID_YO = "YO";
	private static final int YO_SID = 9191;
	private static final String SID_SR = "SR";
	private static final int SR_SID = 9211;
	private static final String SID_ME = "ME";
	private static final int ME_SID = 9221;
	private static final String SID_LS = "LS";
	private static final int LS_SID = 9231;
	private static final String SID_ML = "ML";
	private static final int ML_SID = 9241;
	private static final String SID_KI = "KI";
	private static final int KI_SID = 9271;
	private static final String SID_MA = "MA";
	private static final int MA_SID = 9311;
	private static final String SID_BE = "BE";
	private static final int BE_SID = 9321;
	private static final String SID_BR = "BR";
	private static final int BR_SID = 9331;
	private static final String SID_MO = "MO";
	private static final int MO_SID = 9341;
	private static final String SID_GE = "GE";
	private static final int GE_SID = 9351;
	private static final String SID_AC = "AC";
	private static final int AC_SID = 9371;
	private static final String SID_GL = "GL";
	private static final int GL_SID = 9391;
	private static final String SID_EA = "EA";
	private static final int EA_SID = 9441;
	private static final String SID_LA = "LA";
	private static final int LA_SID = 9601;
	private static final String SID_RI = "RI";
	private static final int RI_SID = 9612;
	private static final String SID_MP = "MP";
	private static final int MP_SID = 9613;
	private static final String SID_RU = "RU";
	private static final int RU_SID = 9614;
	private static final String SID_KC = "KC";
	private static final int KC_SID = 9621;
	private static final String SID_AU = "AU";
	private static final int AU_SID = 9631;
	private static final String SID_NE = "NE";
	private static final int NE_SID = 9641;
	private static final String SID_BD = "BD";
	private static final int BD_SID = 9651;
	private static final String SID_BA = "BA";
	private static final int BA_SID = 9681;
	private static final String SID_AD = "AD";
	private static final int AD_SID = 9691;
	private static final String SID_MK = "MK";
	private static final int MK_SID = 9701;
	private static final String SID_UI = "UI";
	private static final int UI_SID = 9712;
	private static final String SID_MR = "MR";
	private static final int MR_SID = 9721;
	private static final String SID_CE = "CE";
	private static final int CE_SID = 9722;
	private static final String SID_MJ = "MJ";
	private static final int MJ_SID = 9731;
	private static final String SID_ST = "ST";
	private static final int ST_SID = 9741;
	private static final String SID_LI = "LI";
	private static final int LI_SID = 9742;
	private static final String SID_KE = "KE";
	private static final int KE_SID = 9771;
	private static final String SID_JAMES_STR = "JAMES STR";
	private static final int JAMES_STR_SID = 100001;
	private static final String SID_USBT = "USBT";
	private static final int USBT_SID = 52;
	private static final String SID_NI = "NI";
	private static final int NI_SID = 100003;
	private static final String SID_PA = "PA";
	private static final int PA_SID = 311;
	private static final String SID_SCTH = "SCTH";
	private static final int SCTH_SID = 100005;
	private static final String SID_DW = "DW";
	private static final int DW_SID = 100006;
	private static final String SID_CF = "CF";
	private static final int CF_SID = 100011;

	@Override
	public @Nullable Integer convertStopIdFromCodeNotSupported(@NotNull String stopCode) {
		final String stopId = stopCode.trim();
		switch (stopId) {
		case SID_UN:
			return UN_SID;
		case SID_EX:
			return EX_SID;
		case SID_MI:
			return MI_SID;
		case SID_LO:
			return LO_SID;
		case SID_DA:
			return DA_SID;
		case SID_SC:
			return SC_SID;
		case SID_EG:
			return EG_SID;
		case SID_GU:
			return GU_SID;
		case SID_RO:
			return RO_SID;
		case SID_PO:
			return PO_SID;
		case SID_CL:
			return CL_SID;
		case SID_OA:
			return OA_SID;
		case SID_BO:
			return BO_SID;
		case SID_AP:
			return AP_SID;
		case SID_BU:
			return BU_SID;
		case SID_AL:
			return AL_SID;
		case SID_PIN:
			return PIN_SID;
		case SID_AJ:
			return AJ_SID;
		case SID_WH:
			return WH_SID;
		case SID_OS:
			return OS_SID;
		case SID_BL:
			return BL_SID;
		case SID_KP:
			return KP_SID;
		case SID_WE:
			return WE_SID;
		case SID_ET:
			return ET_SID;
		case SID_OR:
			return OR_SID;
		case SID_OL:
			return OL_SID;
		case SID_AG:
			return AG_SID;
		case SID_DI:
			return DI_SID;
		case SID_CO:
			return CO_SID;
		case SID_ER:
			return ER_SID;
		case SID_HA:
			return HA_SID;
		case SID_YO:
			return YO_SID;
		case SID_SR:
			return SR_SID;
		case SID_ME:
			return ME_SID;
		case SID_LS:
			return LS_SID;
		case SID_ML:
			return ML_SID;
		case SID_KI:
			return KI_SID;
		case SID_MA:
			return MA_SID;
		case SID_BE:
			return BE_SID;
		case SID_BR:
			return BR_SID;
		case SID_MO:
			return MO_SID;
		case SID_GE:
			return GE_SID;
		case SID_AC:
			return AC_SID;
		case SID_GL:
			return GL_SID;
		case SID_EA:
			return EA_SID;
		case SID_LA:
			return LA_SID;
		case SID_RI:
			return RI_SID;
		case SID_MP:
			return MP_SID;
		case SID_RU:
			return RU_SID;
		case SID_KC:
			return KC_SID;
		case SID_AU:
			return AU_SID;
		case SID_NE:
			return NE_SID;
		case SID_BD:
			return BD_SID;
		case SID_BA:
			return BA_SID;
		case SID_AD:
			return AD_SID;
		case SID_MK:
			return MK_SID;
		case SID_UI:
			return UI_SID;
		case SID_MR:
			return MR_SID;
		case SID_CE:
			return CE_SID;
		case SID_MJ:
			return MJ_SID;
		case SID_ST:
			return ST_SID;
		case SID_LI:
			return LI_SID;
		case SID_KE:
			return KE_SID;
		case SID_JAMES_STR:
			return JAMES_STR_SID;
		case SID_USBT:
			return USBT_SID;
		case SID_NI:
			return NI_SID;
		case SID_PA:
			return PA_SID;
		case SID_SCTH:
			return SCTH_SID;
		case SID_DW:
			return DW_SID;
		case SID_CF:
			return CF_SID;
		default:
			return super.convertStopIdFromCodeNotSupported(stopCode);
		}
	}
}

package point.zzicback.profile.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Theme {
    PINKY("pinky"),
    CATPPUCCIN("catppuccin"),
    CERBERUS("cerberus"),
    CONCORD("concord"),
    CRIMSON("crimson"),
    FENNEC("fennec"),
    HAMLINDIGO("hamlindigo"),
    LEGACY("legacy"),
    MINT("mint"),
    MODERN("modern"),
    MONA("mona"),
    NOSH("nosh"),
    NOUVEAU("nouveau"),
    PINE("pine"),
    REIGN("reign"),
    ROCKET("rocket"),
    ROSE("rose"),
    SAHARA("sahara"),
    SEAFOAM("seafoam"),
    TERMINUS("terminus"),
    VINTAGE("vintage"),
    VOX("vox"),
    WINTRY("wintry");
    
    private final String value;
}
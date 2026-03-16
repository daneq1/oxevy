# OxeVy

<div align="center">
  
**!!!!UWAGA TEN PROJEKT JEST W BETA!!!!**

**AI bazowany hackclient na OyVey-Ported**

Stworzone z: ChatGPT • DeepSeek • OpenCode • Gemini • Cursor

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Wersja](https://img.shields.io/badge/wersja-1.0.0-niebieski.svg)](https://github.com/daneq1/oxevy)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11+-jasnozielony.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-1.21.11+-pomarańczowy.svg)](https://fabricmc.net/)

</div>

## Podgląd

### OyVey-Ported
![Oryginalny interfejs](images/ui.png)

### OxeVy UI
![Interfejs OxeVy](images/ui1.png)
![Interfejs OxeVy 2](images/ui2.png)

## Funkcje

### Combat
- **KillAura** - Automatyczny atak z konfigurowalnym CPS, czasem odnowienia i synchronizacją rotacji
- **AimBot** - Auto-celowanie na encje
- **Criticals** - Wymuszanie krytycznych ciosów
- **Strafe** - Combat strafe
- **KeyPearl** - Auto-rzucanie perel Ender

### Ruch
- **Flight** - Tryb latania
- **Speed** - Hack prędkości
- **Timer** - Modyfikacja prędkości tyków
- **Step** - Wchodzenie na bloki
- **ReverseStep** - Odwrotny step

### Gracz
- **NoFall** - Zapobieganie obrażeniom od upadku
- **FastPlace** - Szybsze stawianie bloków
- **Velocity** - Modyfikacja odrzutu
- **AutoTotem** - Auto-przełączanie na totem
- **AutoEat** - Auto-jedzenie gdy głodny
- **AirPlace** - Szybsze stawianie bloków w powietrzu
- **FastBreak** - Szybsze łamanie bloków
- **Reach** - Zwiększenie zasięgu ataku

### Render
- **ESP** - ESP encji
- **Tracers** - Linie do encji z konfigurowalnymi pozycjami celu/źródła
- **Nametags** - Własne nametagi
- **Fullbright** - Kontrola jasności
- **BlockHighlight** - Podświetlanie celowanego bloku
- **ChestESP** - Wizualizacja kontenerów
- **HealthBar** - Wyświetlanie pasków zdrowia

### HUD
- **ArrayList** - Lista włączonych modułów z animacjami slajdu
- **Watermark** - Znak wodny klienta
- **MenuWatermark** - Wyświetla w menu głównym
- **Coordinates** - Współrzędne gracza
- **TargetHUD** - Informacje o celu z wyświetlaniem zbroi/głowy
- **ServerInfo** - Szczegóły serwera
- **FPS** - Wyświetlanie FPS

### Klient
- **ClickGui** - GUI konfiguracji modułów z wyszukiwaniem Ctrl+F
- **HudEditor** - Edycja pozycji HUD
- **Notifications** - Powiadomienia modułów

## Wsparcie Multi-Konfiguracji

Zapisuj i wczytuj wiele konfiguracji:
- `.config save <nazwa>` - Zapisz konfigurację
- `.config load <nazwa>` - Wczytaj konfigurację
- `.config list` - Lista konfiguracji
- `.config delete <nazwa>` - Usuń konfigurację

## Użyte Technologie

| AI Narzędzie | Cel |
|--------------|-----|
| ChatGPT | Optymalizacja kodu i implementacja funkcji |
| DeepSeek | Tworzenie promptów |
| OpenCode | Najlepsze praktyki open-source |
| Gemini | Ulepszenia UI/UX |
| Cursor | Pomoc w разработке |

## Wymagania

- Java 21
- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API

## Instalacja

1. Sklonuj repozytorium
   ```bash
   git clone https://github.com/daneq1/oxevy.git
   ```
2. Zbuduj klienta
   ```bash
   ./gradlew build
   ```
3. Znajdź zbudowany JAR w `build/libs/`
4. Wrzuć JAR do folderu `mods`

## Komendy

| Komenda | Opis |
|---------|------|
| `.` | Prefix komend |
| `.help` | Pokaż wszystkie komendy |
| `.toggle <moduł>` | Przełącz moduł |
| `.bind <moduł> <klawisz>` | Przypisz klawisz do modułu |
| `.friend add <nazwa>` | Dodaj znajomego |
| `.friend remove <nazwa>` | Usuń znajomego |
| `.config save` | Zapisz konfigurację |
| `.config load` | Wczytaj konfigurację |

## Zasługi

- OyVey - Bazowy klient
- Fabric Team - Fabric API
- Mixin Team - Mixin

---

Ciekawostka: Ta strona została stworzona przez AI

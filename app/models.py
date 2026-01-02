from __future__ import annotations

from dataclasses import dataclass, field
from datetime import date
from enum import Enum
from typing import Dict, List, Optional


class StatoConsuntivo(str, Enum):
    aperto = "aperto"
    chiuso = "chiuso"


@dataclass
class Documento:
    id: int
    filename: str
    url: str


@dataclass
class VoceSpesa:
    id: int
    descrizione: str
    importo_preventivo: float


@dataclass
class Riparto:
    id: int
    piano_id: int
    voce_id: int
    soggetto: str
    millesimi: int
    quota: float


@dataclass
class Rata:
    id: int
    piano_id: int
    importo: float
    scadenza: date
    descrizione: str


@dataclass
class Consuntivo:
    id: int
    piano_id: int
    totale_speso: float
    stato: StatoConsuntivo = StatoConsuntivo.aperto
    documenti: List[Documento] = field(default_factory=list)
    data_chiusura: Optional[date] = None


@dataclass
class PianoSpesa:
    id: int
    nome: str
    millesimi_totali: int = 1000
    voci: Dict[int, VoceSpesa] = field(default_factory=dict)
    documenti_preventivo: List[Documento] = field(default_factory=list)
    rate: List[Rata] = field(default_factory=list)
    riparti: List[Riparto] = field(default_factory=list)
    consuntivi: List[int] = field(default_factory=list)


@dataclass
class PartecipanteRiparto:
    soggetto: str
    millesimi: int


@dataclass
class PianoSpesaCreate:
    nome: str
    millesimi_totali: int = 1000

    def __post_init__(self) -> None:
        if self.millesimi_totali <= 0:
            raise ValueError("I millesimi totali devono essere positivi")


@dataclass
class VoceSpesaCreate:
    descrizione: str
    importo_preventivo: float

    def __post_init__(self) -> None:
        if self.importo_preventivo <= 0:
            raise ValueError("L'importo preventivo deve essere positivo")


@dataclass
class DocumentoInput:
    filename: str
    url: str


@dataclass
class RipartoInput:
    voce_id: int
    partecipanti: List[PartecipanteRiparto]


@dataclass
class GeneraRateInput:
    totale: float
    numero_rate: int
    prima_scadenza: date
    periodicita: str = "mensile"
    descrizione: str = "Rata"

    def __post_init__(self) -> None:
        if self.numero_rate <= 0:
            raise ValueError("Il numero di rate deve essere positivo")
        if self.totale <= 0:
            raise ValueError("Il totale delle rate deve essere positivo")


@dataclass
class ConsuntivoCreate:
    piano_id: int
    totale_speso: float

    def __post_init__(self) -> None:
        if self.totale_speso <= 0:
            raise ValueError("Il totale speso deve essere positivo")

import threading
import time
from collections import OrderedDict


class TTLCache:
    """스레드 안전한 TTL + 최대 개수 제한 캐시.

    기존 앱은 ViewModel 메모리에 10분 캐시를 둬서 사용자마다, 앱을 켤 때마다
    같은 기사를 다시 분석했다. 서버에 두면 모든 사용자가 결과를 공유한다.
    """

    def __init__(self, ttl_seconds: float, max_items: int = 2000, clock=time.monotonic):
        self.ttl = ttl_seconds
        self.max_items = max_items
        self._clock = clock
        self._data: "OrderedDict[str, tuple[float, object]]" = OrderedDict()
        self._lock = threading.Lock()
        self.hits = 0
        self.misses = 0

    def get(self, key):
        with self._lock:
            item = self._data.get(key)
            if item is None:
                self.misses += 1
                return None
            expires_at, value = item
            if self._clock() >= expires_at:
                del self._data[key]
                self.misses += 1
                return None
            self._data.move_to_end(key)
            self.hits += 1
            return value

    def set(self, key, value) -> None:
        with self._lock:
            self._data[key] = (self._clock() + self.ttl, value)
            self._data.move_to_end(key)
            while len(self._data) > self.max_items:
                self._data.popitem(last=False)

    def stats(self) -> dict:
        with self._lock:
            return {"size": len(self._data), "hits": self.hits, "misses": self.misses}

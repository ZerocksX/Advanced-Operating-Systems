"""
Problem pet filozofa
Problem pet filozofa. Filozofi obavljaju samo dvije različite aktivnosti: misle ili jedu.
To rade na poseban način. Na jednom okruglom stolu nalazi se pet tanjura te pet
štapića (između svaka dva tanjura po jedan). Filozof prilazi stolu, uzima lijevi štapić,
pa desni te jede. Zatim vraća štapiće na stol i odlazi misliti.

Ako rad filozofa predstavimo jednim zadatkom onda se on može opisati na sljedeći način:

filozof i
    ponavljati
        misliti;
        jesti;
    do zauvijek;
Slika 1. Pseudokod zadatka kojeg obavlja proces filozof

Zadatak
Potrebno je pravilno sinkronizirati rad pet procesa filozofa koristeći:

- Lamportov raspodijeljeni protokol (rješavaju studenti čija je predzadnja znamenka JMBAG parna) ili

Svi procesi ispisuju poruku koju šalju i poruku koju primaju.

Sve što u zadatku nije zadano, riješiti na proizvoljan način.
"""
from typing import Optional, Any

'''
Algorithm
Requesting process

Pushing its request in its own queue (ordered by time stamps)
Sending a request to every node.
Waiting for replies from all other nodes.
If own request is at the head of its queue and all replies have been received, enter critical section.
Upon exiting the critical section, remove its request from the queue and send a release message to every process.
Other processes

After receiving a request, pushing the request in its own request queue (ordered by time stamps) and reply with a time stamp.
After receiving release message, remove the corresponding request from its own request queue.
'''

from multiprocessing import Process, Pipe
from multiprocessing.connection import Connection
from itertools import combinations
from random import shuffle, randint
from queue import PriorityQueue, Queue
from heapq import heappush, heappop
from time import time, sleep


class PeekyQueue(PriorityQueue):
    """
    def _init(self, maxsize):
        self.queue = []

    def _qsize(self):
        return len(self.queue)

    def _put(self, item):
        heappush(self.queue, item)

    def _get(self):
        return heappop(self.queue)

    """

    def peek(self):
        # return self.queue[0]
        elem = self.get()
        self.put(elem)
        return elem

    def remove(self, item):
        elems = []
        while not self.empty():
            elems.append(self.get())
        elems.remove(item)
        for e in elems:
            self.put(e)


class IndexedConnection(Connection):

    def __init__(self, connection, id) -> None:
        self.connection = connection
        self.id = id

    def close(self) -> None:
        self.connection.close()

    def fileno(self) -> int:
        return self.connection.fileno()

    def poll(self, timeout: Optional[float] = ...) -> bool:
        return self.connection.poll(timeout)

    def recv(self) -> Any:
        return self.connection.recv()

    def recv_bytes(self, maxlength: Optional[int] = ...) -> bytes:
        return self.connection.recv_bytes(maxlength)

    def recv_bytes_into(self, buf: Any, offset: int = ...) -> int:
        return self.connection.recv_bytes_into(buf, offset)

    def send(self, obj: Any) -> None:
        self.connection.send(obj)

    def send_bytes(self, buf: bytes, offset: int = ..., size: Optional[int] = ...) -> None:
        self.connection.send_bytes(buf, offset, size)


class Type(object):
    request = 1
    response = 2
    exit = 3
    release = 4

    @staticmethod
    def decode(index):
        if index == 1:
            return "request"
        elif index == 2:
            return "response"
        elif index == 3:
            return "exit"
        elif index == 4:
            return "release"
        else:
            return "unknown"


N = 5
start = time()
time_to_run = 1000  # s


class Message(object):

    def __init__(self, type, id, timestamp, target_id) -> None:
        super().__init__()
        self.type = type
        self.id = id
        self.timestamp = timestamp
        self.target_id = target_id

    def __repr__(self) -> str:
        return f"Type: {Type.decode(self.type)}, Id: {self.id}, Clock: {self.timestamp}, Target: {self.target_id}"

    def __eq__(self, other):
        return self.id == other.id and self.type == other.type and self.target_id == other.target_id

    def __ne__(self, other):
        return self.id != other.id or self.type != other.type or self.target_id != other.target_id

    def __lt__(self, other):
        return self.id < other.id if self.timestamp == other.timestamp else self.timestamp < other.timestamp


def request_resource(message: Message, my_id: int, read_pipe: Connection, write_pipes: [Connection],
                     r_qs: {int: PeekyQueue}, timestamp):
    timestamp += 1
    r_q = r_qs[message.target_id]
    r_q.put(message)
    for pipe in write_pipes:
        pipe.send(message)
        print("Philosopher(%d) sending message: (%s) to %d" % (my_id, str(message), pipe.id), flush=True)

    responded = 0
    N = len(write_pipes) + 1
    # print(N, flush=True)
    while not (responded >= N - 1 and r_q.peek() == message):
        if responded == N - 1:
            # print(r_q.peek(), message, r_q.peek() == message, flush=True)
            # r_q_p = []
            # while not r_q.empty():
            #     r_q_p.append(r_q.get())
            # for r in r_q_p:
            #     r_q.put(r)
            # print([e for e in r_q_p], r_q.peek(), flush=True)
            # print([i for i in len(r_q.queue)], r_q.peek(), flush=True)
            pass
        response = read_pipe.recv()
        print("Philosopher(%d) reading message: (%s)" % (my_id, str(response)), flush=True)
        if response.type == Type.request:
            timestamp = max(timestamp, response.timestamp) + 1
            r_qs[response.target_id].put(response)
            new_msg = Message(Type.response, my_id, timestamp, response.target_id)
            print("Philosopher(%d) sending message: (%s) to %d" % (my_id, str(new_msg), response.id), flush=True)

            list(filter(lambda pipe: pipe.id == response.id, write_pipes))[0].send(new_msg)
        elif response.type == Type.response:
            responded += 1
        elif response.type == Type.release:
            # timestamp = max(timestamp, response.timestamp) + 1
            r_qs[response.target_id].remove(Message(Type.request, response.id, response.timestamp, response.target_id))

    # print(r_q.peek(), message, r_q.peek() == message, r_q.peek() == message, flush=True)
    # r_q_p = []
    # while not r_q.empty():
    #     r_q_p.append(r_q.get())
    # for r in r_q_p:
    #     r_q.put(r)
    # print([e for e in r_q_p], r_q.peek(), message, flush=True)
    # r_q.remove(message)
    return timestamp


def release_resource(message: Message, my_id: int, read_pipe: Connection, write_pipes: [Connection],
                     r_qs: {int: PeekyQueue}):
    r_q = r_qs[message.target_id]
    r_q.remove(message)

    release_message = Message(Type.release, my_id, message.timestamp, message.target_id)
    for pipe in write_pipes:
        pipe.send(release_message)
        print("Philosopher(%d) sending message: (%s) to %d" % (my_id, str(release_message), pipe.id), flush=True)


def philosopher(id, read_pipe, write_pipes):
    my_id = id
    print("Philosopher(%d) started" % (my_id,), flush=True)
    # pq = PeekyQueue()
    # r_qs = {i: pq for i in range(-1, N)}
    r_qs = {i: PeekyQueue() for i in range(-1, N)}

    timestamp = randint(0, 5)
    while time() - start < time_to_run:
        # print("before", id, timestamp)
        # message_left = Message(Type.request, my_id, timestamp, (my_id - 1) % N)
        # timestamp = request_resource(message_left, my_id, read_pipe, write_pipes, r_qs, timestamp)
        # print("Philosopher(%d) got %d at %d" % (my_id, message_left.target_id, time()), flush=True)
        # print("mid", id, timestamp)
        # message_right = Message(Type.request, my_id, timestamp, my_id % N)
        # timestamp = request_resource(message_right, my_id, read_pipe, write_pipes, r_qs, timestamp)
        # print("Philosopher(%d) got %d at %d" % (my_id, message_right.target_id, time()), flush=True)
        # print("after", id, timestamp)

        message_table = Message(Type.request, my_id, timestamp, -1)

        timestamp = request_resource(message_table, my_id, read_pipe, write_pipes, r_qs, timestamp)  # approach table
        print("Philosopher(%d) got %d at %d" % (my_id, message_table.target_id, time()), flush=True)

        from random import gauss
        sleep(gauss(1, 0.01))
        f = open('eats-%d.txt' % id, mode='a+')
        msg = ['Philosopher(%d) eating %f\n' % (my_id, time())]
        print(msg, flush=True)
        f.writelines(msg)
        f.close()
        sleep(gauss(1, 0.01))

        print("Philosopher(%d) releasing %d at %d" % (my_id, message_table.target_id, time()), flush=True)

        release_resource(message_table, my_id, read_pipe, write_pipes, r_qs)
        #
        # release_resource(message_right, my_id, read_pipe, write_pipes, r_qs)
        # release_resource(message_left, my_id, read_pipe, write_pipes, r_qs)

    print("Philosopher(%d) finished" % (my_id,), flush=True)


def main():
    # pipes = {}
    # for i, j in combinations(range(N), 2):
    #    pipes[(i, j)] = Pipe()
    pipes = [Pipe() for i in range(N)]
    processes = []
    for i in range(N):

        f = open('eats-%d.txt' % i, mode='w+')
        f.close()
        p = Process(target=philosopher, args=(
            i,
            [IndexedConnection(pipe[0], id) for id, pipe in enumerate(pipes) if id == i][0],
            [IndexedConnection(pipe[1], id) for id, pipe in enumerate(pipes) if id == (i + 1) % N or id == (i - 1) % N],
            # [IndexedConnection(pipe[1], id) for id, pipe in enumerate(pipes) if id == (i + 1) % N or id == (i - 1) % N or id == (i + 2) % N or id == (i - 2) % N],
            # [IndexedConnection(pipe[1], id) for id, pipe in enumerate(pipes) if id != i],
        ))
        processes.append(p)
    order = [i for i in range(N)]
    shuffle(order)
    print(order, flush=True)
    for i in order:
        processes[i].start()
        sleep(0.1)
    for p in processes:
        p.join()
    print("Finished", flush=True)


def test():
    pipeout, pipein = Pipe()
    p = Process(target=philosopher, args=(pipeout,))
    p.start()
    print('Sending message to pipe', flush=True)
    pipein.send([42, None, 'text'])
    p.join()
    pipein.close()
    print('Closed writing pipe', flush=True)


def merge():
    import re
    reg = re.compile('\d+\.\d+')

    pq = PeekyQueue()
    for name in ['eats-%d.txt' % i for i in range(N)]:
        f = open(name, 'r+')
        for line in f.readlines():
            line = line.strip()
            value = reg.findall(line)[0]
            pq.put((value, line))
    while not pq.empty():
        print(pq.get()[1], flush=True)


if __name__ == '__main__':
    # main()
    merge()

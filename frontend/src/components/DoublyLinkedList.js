class Node {
    constructor(data, prev = null, next = null) {
      this.data = data;
      this.prev = prev;
      this.next = next;
    }
  }
  
  export default class DoublyLinkedList {
    constructor() {
      this.head = null;
      this.tail = null;
    }
  
    append(data) {
      const newNode = new Node(data);
      if (!this.head) {
        this.head = newNode;
        this.tail = newNode;
      } else {
        this.tail.next = newNode;
        newNode.prev = this.tail;
        this.tail = newNode;
      }
    }
  
    clear() {
      this.head = null;
      this.tail = null;
    }
  
    toArray() {
      const result = [];
      let current = this.head;
      while (current) {
        result.push(current.data);
        current = current.next;
      }
      return result;
    }
  }
  
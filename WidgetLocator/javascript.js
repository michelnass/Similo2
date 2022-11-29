var attributePriorizationList = ['name', 'class', 'title', 'alt', 'value']; 
var attributeBlackList = ['href', 'src', 'onclick', 'onload', 'tabindex', 'width', 'height', 'style', 'size', 'maxlength'];
var monotoAttributeList = ['id', 'name', 'class', 'title', 'alt', 'value'];
class XPath {constructor(value) {this.value = value;} getValue() {return this.value;} startsWith(value) {return this.value.startsWith(value);} substring(value) {return this.value.substring(value);} headHasAnyPredicates() {return this.value.split('/')[2].includes('[');}
headHasPositionPredicate() {const splitXPath = this.value.split('/'); const regExp = new RegExp('[[0-9]]'); return splitXPath[2].includes('position()') || splitXPath[2].includes('last()') || regExp.test(splitXPath[2]);} headHasTextPredicate() {return this.value.split('/')[2].includes('text()');} addPredicateToHead(predicate) {const splitXPath = this.value.split('/'); splitXPath[2] += predicate; this.value = splitXPath.join('/');} getLength() {const splitXPath = this.value.split('/'); let length = 0; for (const piece of splitXPath) {if (piece) length++;} return length;}}

//function elementIsVisible(el) {if (getComputedStyle(el).visibility === 'hidden' || el.getBoundingClientRect().height == 0) return false; return true;}


// ***** XPath, IdXPath, Robula+, and Montoto ******

function locateElementByXPath(xPath) {const nodesSnapshot = document.evaluate(xPath, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null); if (nodesSnapshot.snapshotLength === 0) return null; return nodesSnapshot.snapshotItem(0);}
function getRobulaPlusXPath(element) {const xPathList = [new XPath('//*')]; while (xPathList.length > 0) {const xPath = xPathList.shift(); let temp = []; temp = temp.concat(transfConvertStar(xPath, element)); temp = temp.concat(transfAddId(xPath, element)); temp = temp.concat(transfAddText(xPath, element)); temp = temp.concat(transfAddAttribute(xPath, element)); temp = temp.concat(transfAddAttributeSet(xPath, element)); temp = temp.concat(transfAddPosition(xPath, element)); temp = temp.concat(transfAddLevel(xPath, element)); temp = [...new Set(temp)]; for (const x of temp) {if (uniquelyLocate(x.getValue(), element)) return x.getValue(); xPathList.push(x);}} return null;}
function uniquelyLocate(xPath, element) {const nodesSnapshot = document.evaluate(xPath, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null); return nodesSnapshot.snapshotLength === 1 && nodesSnapshot.snapshotItem(0) === element;}
function transfConvertStar(xPath, element) {const output = []; const ancestor = getAncestor(element, xPath.getLength() - 1); if (xPath.startsWith('//*')) output.push(new XPath('//' + ancestor.tagName.toLowerCase() + xPath.substring(3))); return output;}
function transfAddId(xPath, element) {const output = []; const ancestor = getAncestor(element, xPath.getLength() - 1); if (ancestor.id && !xPath.headHasAnyPredicates()) {const newXPath = new XPath(xPath.getValue()); newXPath.addPredicateToHead("[@id='"+ancestor.id+"']"); output.push(newXPath);} return output;}
function transfAddText(xPath, element) {const output = []; const ancestor = getAncestor(element, xPath.getLength() - 1); if (ancestor.textContent && !xPath.headHasPositionPredicate() && !xPath.headHasTextPredicate()) {const newXPath = new XPath(xPath.getValue()); newXPath.addPredicateToHead("[contains(text(),'"+cleanText(ancestor.textContent)+"')]"); output.push(newXPath);} return output;}
function transfAddAttribute(xPath, element) {const output = []; const ancestor = getAncestor(element, xPath.getLength() - 1); if (!xPath.headHasAnyPredicates()) {for (const priorityAttribute of attributePriorizationList) {for (const attribute of ancestor.attributes) {if (attribute.name === priorityAttribute) {const newXPath = new XPath(xPath.getValue()); newXPath.addPredicateToHead('[@'+attribute.name+"='"+attribute.value+"']"); output.push(newXPath); break;}}} for (const attribute of ancestor.attributes) {if (!attributeBlackList.includes(attribute.name) && !attributePriorizationList.includes(attribute.name)) {const newXPath = new XPath(xPath.getValue()); newXPath.addPredicateToHead('[@'+attribute.name+"='"+attribute.value+"']"); output.push(newXPath);}}} return output;}
function transfAddAttributeSet(xPath, element) {const output = []; const ancestor = getAncestor(element, xPath.getLength() - 1); if (!xPath.headHasAnyPredicates()) {attributePriorizationList.unshift('id'); let attributes = [...ancestor.attributes]; attributes = attributes.filter(attribute => !attributeBlackList.includes(attribute.name)); let attributePowerSet = generatePowerSet(attributes); attributePowerSet = attributePowerSet.filter(attributeSet => attributeSet.length >= 2); for (const attributeSet of attributePowerSet) {attributeSet.sort(elementCompareFunction.bind(this));} attributePowerSet.sort((set1, set2) => {if (set1.length < set2.length) {return -1;} if (set1.length > set2.length) {return 1;} for (let i = 0; i < set1.length; i++) {if (set1[i] !== set2[i]) {return elementCompareFunction(set1[i], set2[i]);}} return 0;}); attributePriorizationList.shift(); for (const attributeSet of attributePowerSet) {let predicate = '[@'+attributeSet[0].name+"='"+attributeSet[0].value+"'"; for (let i = 1; i < attributeSet.length; i++) {predicate += ' and @'+attributeSet[i].name+"='"+attributeSet[i].value+"'";} predicate += ']'; const newXPath = new XPath(xPath.getValue()); newXPath.addPredicateToHead(predicate); output.push(newXPath);}} return output;}
function transfAddPosition(xPath, element) {const output = []; const ancestor = getAncestor(element, xPath.getLength() - 1); if (!xPath.headHasPositionPredicate()) {let position = 1; if (xPath.startsWith('//*')) {position = Array.from(ancestor.parentNode.children).indexOf(ancestor) + 1;} else {for (const child of ancestor.parentNode.children) {if (ancestor === child) {break;} if (ancestor.tagName === child.tagName) {position++;}}} const newXPath = new XPath(xPath.getValue()); newXPath.addPredicateToHead('['+position+']'); output.push(newXPath);} return output;}
function transfAddLevel(xPath, element) {const output = []; if (xPath.getLength() - 1 < getAncestorCount(element)) {output.push(new XPath('//*' + xPath.substring(1)));} return output;}
function generatePowerSet(input) {return input.reduce((subsets, value) => subsets.concat(subsets.map((set) => [value, ...set])), [[]],);}
function elementCompareFunction(attr1, attr2) {for (const element of attributePriorizationList) {if (element === attr1.name) {return -1;} if (element === attr2.name) {return 1;}} return 0;}
function getAncestor(element, index) {let output = element; for (let i = 0; i < index; i++) {output = output.parentElement;} return output;}
function getAncestorCount(element) {let count = 0; while (element.parentElement) {element = element.parentElement; count++;} return count;}
function getXPosition(el) {return el.getBoundingClientRect().left;}
function getYPosition(el) {return el.getBoundingClientRect().top+window.scrollY;}
function getMaxWidth(el) {return el.getBoundingClientRect().width;}
function getMaxHeight(el) {return el.getBoundingClientRect().height;}
//function elementIsVisible(el) {if (getComputedStyle(el).visibility === 'hidden') return false; return true;}
function elementIsVisible(el) {if (getComputedStyle(el).visibility === 'hidden' || el.getBoundingClientRect().height == 0) return false; return true;}
function nullToEmpty(cStr) {return cStr == null ? [] : cStr;}
function getNodePosition(node) {if (!node.parentNode) return -1; var siblings = node.parentNode.childNodes, count = 0, position; for (var i = 0; i < siblings.length; i++) {var object = siblings[i]; if(object.nodeType == node.nodeType && object.nodeName == node.nodeName) {count++; if(object == node) position = count;}} return position;}
function getXPath(element) {const idx = (sib, name) => sib ? idx(sib.previousElementSibling, name||sib.localName) + (sib.localName == name): 1; const segs = elm => !elm || elm.nodeType !== 1 ? ['']: [...segs(elm.parentNode), elm instanceof HTMLElement? `${elm.localName}[${idx(elm)}]`: `*[local-name() = "${elm.localName}"][${idx(elm)}]`]; return segs(element).join('/');}
//function getIdXPath(element) {const idx = (sib, name) => sib ? idx(sib.previousElementSibling, name||sib.localName) + (sib.localName == name): 1; const segs = elm => !elm || elm.nodeType !== 1 ? ['']: elm.id && document.getElementById(elm.id) === elm ? [`id("${elm.id}")`]: [...segs(elm.parentNode), elm instanceof HTMLElement? `${elm.localName}[${idx(elm)}]`: `*[local-name() = "${elm.localName}"][${idx(elm)}]`]; return segs(element).join('/');}
function getIdXPath(element) {const idx = (sib, name) => sib ? idx(sib.previousElementSibling, name||sib.localName) + (sib.localName == name): 1; const segs = elm => !elm || elm.nodeType !== 1 ? ['']: elm.id && document.getElementById(elm.id) === elm ? [`//*[@id='${elm.id}']`]: [...segs(elm.parentNode), elm instanceof HTMLElement? `${elm.localName}[${idx(elm)}]`: `*[local-name() = "${elm.localName}"][${idx(elm)}]`]; return segs(element).join('/');}
function escapeQuotes(s) {return s.replace('\'', ' ');}
function cleanText(s) {return s.replace(/[^\w\s]/gi, '');}
function getMonotoXPath(element) {var x=''; var n=element; while(true) {if ((n.children.length==0 || n.children.length==1 && n.children[0].nodeType==3) && n.textContent.length>0) {let xpath='//'+n.tagName+"[text()='"+escapeQuotes(n.textContent)+"']"+x; if (uniquelyLocate(xpath, element)) return xpath;} else {let xpath='//'+n.tagName+x; if (uniquelyLocate(xpath, element)) return xpath;} var e=''; for (const attribute of monotoAttributeList) {var attr=n.getAttributeNode(attribute); if(attr && attr.value.length>0) {if (e.length>0) e+=' and '; e+='@'+attr.name+"='"+attr.value+"'"; let xpath='/'+n.tagName+'['+e+']'+x; if (uniquelyLocate(xpath, element)) return xpath;}} if ((n.children.length==0 || n.children.length==1 && n.children[0].nodeType==3) && n.textContent.length>0) {if (e.length>0) e+=' and '; e+="text()='"+escapeQuotes(n.textContent)+"'";} if (e.length>0) x='/'+n.tagName+'['+e+']'+x; else x='/'+n.tagName+x; let xpath='/'+x; if (uniquelyLocate(xpath, element)) return xpath; n=n.parentElement; if (n==null) return null;}}


// ***** Selenium IDE *****

function cssDataAttr(e) {
  const dataAttributes = ['data-test', 'data-test-id']
  for (let i = 0; i < dataAttributes.length; i++) {
    const attr = dataAttributes[i]
    const value = e.getAttribute(attr)
    if (attr & value) {
      return `*[${attr}="${value}"]`
    }
  }
  return null
}

function id(e) {
if (e.id) {
  return e.id
  }
  return null
}

function linkText(e) {
  if (e.nodeName == 'A') {
    let text = e.textContent
    if (!text.match(/^\s*$/)) {
      return (
        text.replace(/\xA0/g, ' ').replace(/^\s*(.*?)\s*$/, '$1')
      )
    }
  }
  return null
}

function name(e) {
if (e.name) {
  return e.name
  }
  return null
}

function xpathHtmlElement(name) {
  if (document.contentType == 'application/xhtml+xml') {
    // "x:" prefix is required when testing XHTML pages
    return 'x:' + name
  } else {
    return name
  }
}

function findElement(path) {
  return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
}

function preciseXPath(xpath, e) {
  //only create more precise xpath if needed
  if (findElement(xpath) != e) {
    let result = e.ownerDocument.evaluate(
      xpath,
      e.ownerDocument,
      null,
      XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,
      null
    )
    //skip first element (result:0 xpath index:1)
    for (let i = 0, len = result.snapshotLength; i < len; i++) {
      let newPath = '(' + xpath + ')[' + (i + 1) + ']'
      if (findElement(newPath) == e) {
        return newPath
      }
    }
  }
  return xpath
}

function attributeValue(value) {
  if (value.indexOf("'") < 0) {
    return "'" + value + "'"
  } else if (value.indexOf('"') < 0) {
    return '"' + value + '"'
  } else {
    let result = 'concat('
    let part = ''
    let didReachEndOfValue = false
    while (!didReachEndOfValue) {
      let apos = value.indexOf("'")
      let quot = value.indexOf('"')
      if (apos < 0) {
        result += "'" + value + "'"
        didReachEndOfValue = true
        break
      } else if (quot < 0) {
        result += '"' + value + '"'
        didReachEndOfValue = true
        break
      } else if (quot < apos) {
        part = value.substring(0, apos)
        result += "'" + part + "'"
        value = value.substring(part.length)
      } else {
        part = value.substring(0, quot)
        result += '"' + part + '"'
        value = value.substring(part.length)
      }
      result += ','
    }
    result += ')'
    return result
  }
}

function xpathLink(e) {
  if (e.nodeName == 'A') {
    let text = e.textContent
    if (!text.match(/^\s*$/)) {
      return preciseXPath(
        '//' +
          xpathHtmlElement('a') +
          "[contains(text(),'" +
          text.replace(/^\s+/, '').replace(/\s+$/, '') +
          "')]",
        e
      )
    }
  }
  return null
}

function xpathImg(e) {
  if (e.nodeName == 'IMG') {
    if (e.alt != '') {
      return preciseXPath(
        '//' +
          xpathHtmlElement('img') +
          '[@alt=' +
          attributeValue(e.alt) +
          ']',
        e
      )
    } else if (e.title != '') {
      return preciseXPath(
        '//' +
          xpathHtmlElement('img') +
          '[@title=' +
          attributeValue(e.title) +
          ']',
        e
      )
    } else if (e.src != '') {
      return preciseXPath(
        '//' +
          xpathHtmlElement('img') +
          '[contains(@src,' +
          attributeValue(e.src) +
          ')]',
        e
      )
    }
  }
  return null
}

function xpathAttr(e) {
  const PREFERRED_ATTRIBUTES = [
    'id',
    'name',
    'value',
    'type',
    'action',
    'onclick',
  ]
  let i = 0

  function attributesXPath(name, attNames, attributes) {
    let locator = '//' + xpathHtmlElement(name) + '['
    for (i = 0; i < attNames.length; i++) {
      if (i > 0) {
        locator += ' and '
      }
      let attName = attNames[i]
      locator += '@' + attName + '=' + attributeValue(attributes[attName])
    }
    locator += ']'
    return preciseXPath(locator, e)
  }

  if (e.attributes) {
    let atts = e.attributes
    let attsMap = {}
    for (i = 0; i < atts.length; i++) {
      let att = atts[i]
      attsMap[att.name] = att.value
    }
    let names = []
    // try preferred attributes
    for (i = 0; i < PREFERRED_ATTRIBUTES.length; i++) {
      let name = PREFERRED_ATTRIBUTES[i]
      if (attsMap[name] != null) {
        names.push(name)
        let locator = attributesXPath.call(
          this,
          e.nodeName.toLowerCase(),
          names,
          attsMap
        )
        if (e == findElement(locator)) {
          return locator
        }
      }
    }
  }
  return null
}

function getNodeNbr(current) {
  let childNodes = current.parentNode.childNodes
  let total = 0
  let index = -1
  for (let i = 0; i < childNodes.length; i++) {
    let child = childNodes[i]
    if (child.nodeName == current.nodeName) {
      if (child == current) {
        index = total
      }
      total++
    }
  }
  return index
}

function relativeXPathFromParent(current) {
  let index = getNodeNbr(current)
  let currentPath = '/' + xpathHtmlElement(current.nodeName.toLowerCase())
  if (index > 0) {
    currentPath += '[' + (index + 1) + ']'
  }
  return currentPath
}

function xpathIdRelative(e) {
  let path = ''
  let current = e
  while (current != null) {
    if (current.parentNode != null) {
      path = relativeXPathFromParent(current) + path
      if (
        1 == current.parentNode.nodeType && // ELEMENT_NODE
        current.parentNode.getAttribute('id')
      ) {
        return preciseXPath(
          '//' +
            xpathHtmlElement(current.parentNode.nodeName.toLowerCase()) +
            '[@id=' +
            attributeValue(current.parentNode.getAttribute('id')) +
            ']' +
            path,
          e
        )
      }
    } else {
      return null
    }
    current = current.parentNode
  }
  return null
}

function xpathHref(e) {
  if (e.attributes && e.hasAttribute('href')) {
    let href = e.getAttribute('href')
    if (href.search(/^http?:\/\//) >= 0) {
      return preciseXPath(
        '//' +
          xpathHtmlElement('a') +
          '[@href=' +
          attributeValue(href) +
          ']',
        e
      )
    } else {
      // use contains(), because in IE getAttribute("href") will return absolute path
      return preciseXPath(
        '//' +
          xpathHtmlElement('a') +
          '[contains(@href, ' +
          attributeValue(href) +
          ')]',
        e
      )
    }
  }
  return null
}

function xpathPosition(e, opt_contextNode) {
  let path = ''
  let current = e
  while (current != null && current != opt_contextNode) {
    let currentPath
    if (current.parentNode != null) {
      currentPath = relativeXPathFromParent(current)
    } else {
      currentPath = '/' + xpathHtmlElement(current.nodeName.toLowerCase())
    }
    path = currentPath + path
    let locator = '/' + path
    if (e == findElement(locator)) {
      return locator
    }
    current = current.parentNode
  }
  return null
}

function xpathInnerText(el) {
  if (el.innerText) {
    return `xpath=//${el.nodeName.toLowerCase()}[contains(.,'${el.innerText}')]`
  } else {
    return null
  }
}

function getElementByXpath(path) {
  return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
}

function getElementsByXpath(path) {
  return document.evaluate(path, document, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
}

function getSeleniumIDELocator(e) {
var locator = null;
//locator = cssDataAttr(e);
//if (locator != null) return "css:"+locator;
locator = id(e);
if (locator != null) return "id:"+locator;
locator = linkText(e);
if (locator != null) return "linkText:"+locator;
locator = name(e);
if (locator != null) return "name:"+locator;
locator = xpathLink(e);
if (locator != null) return "xpath:"+locator;
locator = xpathImg(e);
if (locator != null) return "xpath:"+locator;
locator = xpathAttr(e);
if (locator != null) return "xpath:"+locator;
locator = xpathIdRelative(e);
if (locator != null) return "xpath:"+locator;
locator = xpathHref(e);
if (locator != null) return "xpath:"+locator;
locator = xpathPosition(e, null);
if (locator != null) return "xpath:"+locator;
locator = xpathInnerText(e);
if (locator != null) return "xpath:"+locator;
}

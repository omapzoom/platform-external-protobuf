// Protocol Buffers - Google's data interchange format
// Copyright 2013 Google Inc.  All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.google.protobuf;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.EnumClassNanoMultiple;
import com.google.protobuf.nano.EnumClassNanos;
import com.google.protobuf.nano.EnumValidity;
import com.google.protobuf.nano.EnumValidityAccessors;
import com.google.protobuf.nano.Extensions;
import com.google.protobuf.nano.Extensions.AnotherMessage;
import com.google.protobuf.nano.Extensions.MessageWithGroup;
import com.google.protobuf.nano.FileScopeEnumMultiple;
import com.google.protobuf.nano.FileScopeEnumRefNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.MessageScopeEnumRefNano;
import com.google.protobuf.nano.MultipleImportingNonMultipleNano1;
import com.google.protobuf.nano.MultipleImportingNonMultipleNano2;
import com.google.protobuf.nano.MultipleNameClashNano;
import com.google.protobuf.nano.NanoAccessorsOuterClass.TestNanoAccessors;
import com.google.protobuf.nano.NanoHasOuterClass.TestAllTypesNanoHas;
import com.google.protobuf.nano.NanoOuterClass;
import com.google.protobuf.nano.NanoOuterClass.TestAllTypesNano;
import com.google.protobuf.nano.NanoReferenceTypes;
import com.google.protobuf.nano.NanoRepeatedPackables;
import com.google.protobuf.nano.TestRepeatedMergeNano;
import com.google.protobuf.nano.UnittestImportNano;
import com.google.protobuf.nano.UnittestMultipleNano;
import com.google.protobuf.nano.UnittestRecursiveNano.RecursiveMessageNano;
import com.google.protobuf.nano.UnittestSimpleNano.SimpleMessageNano;
import com.google.protobuf.nano.UnittestSingleNano.SingleMessageNano;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Test nano runtime.
 *
 * @author ulas@google.com Ulas Kirazci
 */
public class NanoTest extends TestCase {
  @Override
  public void setUp() throws Exception {
  }

  public void testSimpleMessageNano() throws Exception {
    SimpleMessageNano msg = new SimpleMessageNano();
    assertEquals(123, msg.d);
    assertEquals(null, msg.nestedMsg);
    assertEquals(SimpleMessageNano.BAZ, msg.defaultNestedEnum);

    msg.d = 456;
    assertEquals(456, msg.d);

    SimpleMessageNano.NestedMessage nestedMsg = new SimpleMessageNano.NestedMessage();
    nestedMsg.bb = 2;
    assertEquals(2, nestedMsg.bb);
    msg.nestedMsg = nestedMsg;
    assertEquals(2, msg.nestedMsg.bb);

    msg.defaultNestedEnum = SimpleMessageNano.BAR;
    assertEquals(SimpleMessageNano.BAR, msg.defaultNestedEnum);

    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    SimpleMessageNano newMsg = SimpleMessageNano.parseFrom(result);
    assertEquals(456, newMsg.d);
    assertEquals(2, msg.nestedMsg.bb);
    assertEquals(SimpleMessageNano.BAR, msg.defaultNestedEnum);
  }

  public void testRecursiveMessageNano() throws Exception {
    RecursiveMessageNano msg = new RecursiveMessageNano();
    assertTrue(msg.repeatedRecursiveMessageNano.length == 0);

    RecursiveMessageNano msg1 = new RecursiveMessageNano();
    msg1.id = 1;
    assertEquals(1, msg1.id);
    RecursiveMessageNano msg2 = new RecursiveMessageNano();
    msg2.id = 2;
    RecursiveMessageNano msg3 = new RecursiveMessageNano();
    msg3.id = 3;

    RecursiveMessageNano.NestedMessage nestedMsg = new RecursiveMessageNano.NestedMessage();
    nestedMsg.a = msg1;
    assertEquals(1, nestedMsg.a.id);

    msg.id = 0;
    msg.nestedMessage = nestedMsg;
    msg.optionalRecursiveMessageNano = msg2;
    msg.repeatedRecursiveMessageNano = new RecursiveMessageNano[] { msg3 };

    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 16);
    assertEquals(result.length, msgSerializedSize);

    RecursiveMessageNano newMsg = RecursiveMessageNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedRecursiveMessageNano.length);

    assertEquals(0, newMsg.id);
    assertEquals(1, newMsg.nestedMessage.a.id);
    assertEquals(2, newMsg.optionalRecursiveMessageNano.id);
    assertEquals(3, newMsg.repeatedRecursiveMessageNano[0].id);
  }

  public void testNanoRequiredInt32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.id = 123;
    assertEquals(123, msg.id);
    msg.clear().id = 456;
    assertEquals(456, msg.id);
    msg.clear();

    msg.id = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 3);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.id);
  }

  public void testNanoOptionalInt32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalInt32 = 123;
    assertEquals(123, msg.optionalInt32);
    msg.clear()
       .optionalInt32 = 456;
    assertEquals(456, msg.optionalInt32);
    msg.clear();

    msg.optionalInt32 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 5);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalInt32);
  }

  public void testNanoOptionalInt64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalInt64 = 123;
    assertEquals(123, msg.optionalInt64);
    msg.clear()
       .optionalInt64 = 456;
    assertEquals(456, msg.optionalInt64);
    msg.clear();
    assertEquals(0, msg.optionalInt64);

    msg.optionalInt64 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 5);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalInt64);
  }

  public void testNanoOptionalUint32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalUint32 = 123;
    assertEquals(123, msg.optionalUint32);
    msg.clear()
       .optionalUint32 = 456;
    assertEquals(456, msg.optionalUint32);
    msg.clear();
    assertEquals(0, msg.optionalUint32);

    msg.optionalUint32 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 5);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalUint32);
  }

  public void testNanoOptionalUint64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalUint64 = 123;
    assertEquals(123, msg.optionalUint64);
    msg.clear()
       .optionalUint64 = 456;
    assertEquals(456, msg.optionalUint64);
    msg.clear();
    assertEquals(0, msg.optionalUint64);

    msg.optionalUint64 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 5);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalUint64);
  }

  public void testNanoOptionalSint32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalSint32 = 123;
    assertEquals(123, msg.optionalSint32);
    msg.clear()
       .optionalSint32 = 456;
    assertEquals(456, msg.optionalSint32);
    msg.clear();
    assertEquals(0, msg.optionalSint32);

    msg.optionalSint32 = -123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(-123, newMsg.optionalSint32);
  }

  public void testNanoOptionalSint64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalSint64 = 123;
    assertEquals(123, msg.optionalSint64);
    msg.clear()
       .optionalSint64 = 456;
    assertEquals(456, msg.optionalSint64);
    msg.clear();
    assertEquals(0, msg.optionalSint64);

    msg.optionalSint64 = -123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(-123, newMsg.optionalSint64);
  }

  public void testNanoOptionalFixed32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalFixed32 = 123;
    assertEquals(123, msg.optionalFixed32);
    msg.clear()
       .optionalFixed32 = 456;
    assertEquals(456, msg.optionalFixed32);
    msg.clear();
    assertEquals(0, msg.optionalFixed32);

    msg.optionalFixed32 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalFixed32);
  }

  public void testNanoOptionalFixed64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalFixed64 = 123;
    assertEquals(123, msg.optionalFixed64);
    msg.clear()
       .optionalFixed64 = 456;
    assertEquals(456, msg.optionalFixed64);
    msg.clear();
    assertEquals(0, msg.optionalFixed64);

    msg.optionalFixed64 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 12);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalFixed64);
  }

  public void testNanoOptionalSfixed32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalSfixed32 = 123;
    assertEquals(123, msg.optionalSfixed32);
    msg.clear()
       .optionalSfixed32 = 456;
    assertEquals(456, msg.optionalSfixed32);
    msg.clear();
    assertEquals(0, msg.optionalSfixed32);

    msg.optionalSfixed32 = 123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(123, newMsg.optionalSfixed32);
  }

  public void testNanoOptionalSfixed64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalSfixed64 = 123;
    assertEquals(123, msg.optionalSfixed64);
    msg.clear()
       .optionalSfixed64 = 456;
    assertEquals(456, msg.optionalSfixed64);
    msg.clear();
    assertEquals(0, msg.optionalSfixed64);

    msg.optionalSfixed64 = -123;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 12);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(-123, newMsg.optionalSfixed64);
  }

  public void testNanoOptionalFloat() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalFloat = 123f;
    assertTrue(123.0f == msg.optionalFloat);
    msg.clear()
       .optionalFloat = 456.0f;
    assertTrue(456.0f == msg.optionalFloat);
    msg.clear();
    assertTrue(0.0f == msg.optionalFloat);

    msg.optionalFloat = -123.456f;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(-123.456f == newMsg.optionalFloat);
  }

  public void testNanoOptionalDouble() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalDouble = 123;
    assertTrue(123.0 == msg.optionalDouble);
    msg.clear()
       .optionalDouble = 456.0;
    assertTrue(456.0 == msg.optionalDouble);
    msg.clear();
    assertTrue(0.0 == msg.optionalDouble);

    msg.optionalDouble = -123.456;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 12);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(-123.456 == newMsg.optionalDouble);
  }

  public void testNanoOptionalBool() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalBool = true;
    assertTrue(msg.optionalBool);
    msg.clear()
       .optionalBool = true;
    assertTrue(msg.optionalBool);
    msg.clear();
    assertFalse(msg.optionalBool);

    msg.optionalBool = true;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 5);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalBool);
  }

  public void testNanoOptionalString() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalString = "hello";
    assertEquals("hello", msg.optionalString);
    msg.clear();
    assertTrue(msg.optionalString.isEmpty());
    msg.clear()
       .optionalString = "hello2";
    assertEquals("hello2", msg.optionalString);
    msg.clear();
    assertTrue(msg.optionalString.isEmpty());

    msg.optionalString = "bye";
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalString != null);
    assertEquals("bye", newMsg.optionalString);
  }

  public void testNanoOptionalBytes() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertFalse(msg.optionalBytes.length > 0);
    msg.optionalBytes = InternalNano.copyFromUtf8("hello");
    assertTrue(msg.optionalBytes.length > 0);
    assertEquals("hello", new String(msg.optionalBytes, "UTF-8"));
    msg.clear();
    assertFalse(msg.optionalBytes.length > 0);
    msg.clear()
       .optionalBytes = InternalNano.copyFromUtf8("hello");
    assertTrue(msg.optionalBytes.length > 0);
    msg.clear();
    assertFalse(msg.optionalBytes.length > 0);

    msg.optionalBytes = InternalNano.copyFromUtf8("bye");
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalBytes.length > 0);
    assertEquals("bye", new String(newMsg.optionalBytes, "UTF-8"));
  }

  public void testNanoOptionalGroup() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    TestAllTypesNano.OptionalGroup grp = new TestAllTypesNano.OptionalGroup();
    grp.a = 1;
    assertFalse(msg.optionalGroup != null);
    msg.optionalGroup = grp;
    assertTrue(msg.optionalGroup != null);
    assertEquals(1, msg.optionalGroup.a);
    msg.clear();
    assertFalse(msg.optionalGroup != null);
    msg.clear()
       .optionalGroup = new TestAllTypesNano.OptionalGroup();
    msg.optionalGroup.a = 2;
    assertTrue(msg.optionalGroup != null);
    msg.clear();
    assertFalse(msg.optionalGroup != null);

    msg.optionalGroup = grp;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 10);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalGroup != null);
    assertEquals(1, newMsg.optionalGroup.a);
  }

  public void testNanoOptionalGroupWithUnknownFieldsEnabled() throws Exception {
    MessageWithGroup msg = new MessageWithGroup();
    MessageWithGroup.Group grp = new MessageWithGroup.Group();
    grp.a = 1;
    msg.group = grp;
    byte [] serialized = MessageNano.toByteArray(msg);

    MessageWithGroup parsed = MessageWithGroup.parseFrom(serialized);
    assertTrue(msg.group != null);
    assertEquals(1, msg.group.a);

    byte [] serialized2 = MessageNano.toByteArray(parsed);
    assertEquals(serialized2.length, serialized.length);
    MessageWithGroup parsed2 = MessageWithGroup.parseFrom(serialized2);
  }

  public void testNanoOptionalNestedMessage() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    TestAllTypesNano.NestedMessage nestedMsg = new TestAllTypesNano.NestedMessage();
    nestedMsg.bb = 1;
    assertFalse(msg.optionalNestedMessage != null);
    msg.optionalNestedMessage = nestedMsg;
    assertTrue(msg.optionalNestedMessage != null);
    assertEquals(1, msg.optionalNestedMessage.bb);
    msg.clear();
    assertFalse(msg.optionalNestedMessage != null);
    msg.clear()
       .optionalNestedMessage = new TestAllTypesNano.NestedMessage();
    msg.optionalNestedMessage.bb = 2;
    assertTrue(msg.optionalNestedMessage != null);
    msg.clear();
    assertFalse(msg.optionalNestedMessage != null);

    msg.optionalNestedMessage = nestedMsg;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalNestedMessage != null);
    assertEquals(1, newMsg.optionalNestedMessage.bb);
  }

  public void testNanoOptionalForeignMessage() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    NanoOuterClass.ForeignMessageNano nestedMsg = new NanoOuterClass.ForeignMessageNano();
    nestedMsg.c = 1;
    assertFalse(msg.optionalForeignMessage != null);
    msg.optionalForeignMessage = nestedMsg;
    assertTrue(msg.optionalForeignMessage != null);
    assertEquals(1, msg.optionalForeignMessage.c);
    msg.clear();
    assertFalse(msg.optionalForeignMessage != null);
    msg.clear()
       .optionalForeignMessage = new NanoOuterClass.ForeignMessageNano();
    msg.optionalForeignMessage.c = 2;
    assertTrue(msg.optionalForeignMessage != null);
    msg.clear();
    assertFalse(msg.optionalForeignMessage != null);

    msg.optionalForeignMessage = nestedMsg;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalForeignMessage != null);
    assertEquals(1, newMsg.optionalForeignMessage.c);
  }

  public void testNanoOptionalImportMessage() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    UnittestImportNano.ImportMessageNano nestedMsg = new UnittestImportNano.ImportMessageNano();
    nestedMsg.d = 1;
    assertFalse(msg.optionalImportMessage != null);
    msg.optionalImportMessage = nestedMsg;
    assertTrue(msg.optionalImportMessage != null);
    assertEquals(1, msg.optionalImportMessage.d);
    msg.clear();
    assertFalse(msg.optionalImportMessage != null);
    msg.clear()
       .optionalImportMessage = new UnittestImportNano.ImportMessageNano();
    msg.optionalImportMessage.d = 2;
    assertTrue(msg.optionalImportMessage != null);
    msg.clear();
    assertFalse(msg.optionalImportMessage != null);

    msg.optionalImportMessage = nestedMsg;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalImportMessage != null);
    assertEquals(1, newMsg.optionalImportMessage.d);
  }

  public void testNanoOptionalNestedEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalNestedEnum = TestAllTypesNano.BAR;
    assertEquals(TestAllTypesNano.BAR, msg.optionalNestedEnum);
    msg.clear()
       .optionalNestedEnum = TestAllTypesNano.BAZ;
    assertEquals(TestAllTypesNano.BAZ, msg.optionalNestedEnum);
    msg.clear();
    assertEquals(TestAllTypesNano.FOO, msg.optionalNestedEnum);

    msg.optionalNestedEnum = TestAllTypesNano.BAR;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(TestAllTypesNano.BAR, newMsg.optionalNestedEnum);
  }

  public void testNanoOptionalForeignEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalForeignEnum = NanoOuterClass.FOREIGN_NANO_BAR;
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAR, msg.optionalForeignEnum);
    msg.clear()
       .optionalForeignEnum = NanoOuterClass.FOREIGN_NANO_BAZ;
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAZ, msg.optionalForeignEnum);
    msg.clear();
    assertEquals(NanoOuterClass.FOREIGN_NANO_FOO, msg.optionalForeignEnum);

    msg.optionalForeignEnum = NanoOuterClass.FOREIGN_NANO_BAR;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAR, newMsg.optionalForeignEnum);
  }

  public void testNanoOptionalImportEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalImportEnum = UnittestImportNano.IMPORT_NANO_BAR;
    assertEquals(UnittestImportNano.IMPORT_NANO_BAR, msg.optionalImportEnum);
    msg.clear()
       .optionalImportEnum = UnittestImportNano.IMPORT_NANO_BAZ;
    assertEquals(UnittestImportNano.IMPORT_NANO_BAZ, msg.optionalImportEnum);
    msg.clear();
    assertEquals(UnittestImportNano.IMPORT_NANO_FOO, msg.optionalImportEnum);

    msg.optionalImportEnum = UnittestImportNano.IMPORT_NANO_BAR;
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(UnittestImportNano.IMPORT_NANO_BAR, newMsg.optionalImportEnum);
  }

  public void testNanoOptionalStringPiece() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalStringPiece = "hello";
    assertEquals("hello", msg.optionalStringPiece);
    msg.clear();
    assertTrue(msg.optionalStringPiece.isEmpty());
    msg.clear()
       .optionalStringPiece = "hello2";
    assertEquals("hello2", msg.optionalStringPiece);
    msg.clear();
    assertTrue(msg.optionalStringPiece.isEmpty());

    msg.optionalStringPiece = "bye";
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalStringPiece != null);
    assertEquals("bye", newMsg.optionalStringPiece);
  }

  public void testNanoOptionalCord() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalCord = "hello";
    assertEquals("hello", msg.optionalCord);
    msg.clear();
    assertTrue(msg.optionalCord.isEmpty());
    msg.clear()
       .optionalCord = "hello2";
    assertEquals("hello2", msg.optionalCord);
    msg.clear();
    assertTrue(msg.optionalCord.isEmpty());

    msg.optionalCord = "bye";
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertTrue(newMsg.optionalCord != null);
    assertEquals("bye", newMsg.optionalCord);
  }

  public void testNanoRepeatedInt32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedInt32.length);
    msg.repeatedInt32 = new int[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedInt32[1]);
    assertEquals(456, msg.repeatedInt32[2]);
    msg.clear();
    assertEquals(0, msg.repeatedInt32.length);
    msg.clear()
       .repeatedInt32 = new int[] { 456 };
    assertEquals(1, msg.repeatedInt32.length);
    assertEquals(456, msg.repeatedInt32[0]);
    msg.clear();
    assertEquals(0, msg.repeatedInt32.length);

    // Test 1 entry
    msg.clear()
       .repeatedInt32 = new int[] { 123 };
    assertEquals(1, msg.repeatedInt32.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedInt32.length);
    assertEquals(123, newMsg.repeatedInt32[0]);

    // Test 2 entries
    msg.clear()
       .repeatedInt32 = new int[] { 123, 456 };
    assertEquals(2, msg.repeatedInt32.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 10);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedInt32.length);
    assertEquals(123, newMsg.repeatedInt32[0]);
    assertEquals(456, newMsg.repeatedInt32[1]);
  }

  public void testNanoRepeatedInt64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedInt64.length);
    msg.repeatedInt64 = new long[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedInt64[1]);
    assertEquals(456, msg.repeatedInt64[2]);
    msg.clear();
    assertEquals(0, msg.repeatedInt64.length);
    msg.clear()
       .repeatedInt64 = new long[] { 456 };
    assertEquals(1, msg.repeatedInt64.length);
    assertEquals(456, msg.repeatedInt64[0]);
    msg.clear();
    assertEquals(0, msg.repeatedInt64.length);

    // Test 1 entry
    msg.clear()
       .repeatedInt64 = new long[] { 123 };
    assertEquals(1, msg.repeatedInt64.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedInt64.length);
    assertEquals(123, newMsg.repeatedInt64[0]);

    // Test 2 entries
    msg.clear()
       .repeatedInt64 = new long[] { 123, 456 };
    assertEquals(2, msg.repeatedInt64.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 10);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedInt64.length);
    assertEquals(123, newMsg.repeatedInt64[0]);
    assertEquals(456, newMsg.repeatedInt64[1]);
  }

  public void testNanoRepeatedUint32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedUint32.length);
    msg.repeatedUint32 = new int[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedUint32[1]);
    assertEquals(456, msg.repeatedUint32[2]);
    msg.clear();
    assertEquals(0, msg.repeatedUint32.length);
    msg.clear()
       .repeatedUint32 = new int[] { 456 };
    assertEquals(1, msg.repeatedUint32.length);
    assertEquals(456, msg.repeatedUint32[0]);
    msg.clear();
    assertEquals(0, msg.repeatedUint32.length);

    // Test 1 entry
    msg.clear()
       .repeatedUint32 = new int[] { 123 };
    assertEquals(1, msg.repeatedUint32.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedUint32.length);
    assertEquals(123, newMsg.repeatedUint32[0]);

    // Test 2 entries
    msg.clear()
       .repeatedUint32 = new int[] { 123, 456 };
    assertEquals(2, msg.repeatedUint32.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 10);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedUint32.length);
    assertEquals(123, newMsg.repeatedUint32[0]);
    assertEquals(456, newMsg.repeatedUint32[1]);
  }

  public void testNanoRepeatedUint64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedUint64.length);
    msg.repeatedUint64 = new long[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedUint64[1]);
    assertEquals(456, msg.repeatedUint64[2]);
    msg.clear();
    assertEquals(0, msg.repeatedUint64.length);
    msg.clear()
       .repeatedUint64 = new long[] { 456 };
    assertEquals(1, msg.repeatedUint64.length);
    assertEquals(456, msg.repeatedUint64[0]);
    msg.clear();
    assertEquals(0, msg.repeatedUint64.length);

    // Test 1 entry
    msg.clear()
       .repeatedUint64 = new long[] { 123 };
    assertEquals(1, msg.repeatedUint64.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedUint64.length);
    assertEquals(123, newMsg.repeatedUint64[0]);

    // Test 2 entries
    msg.clear()
       .repeatedUint64 = new long[] { 123, 456 };
    assertEquals(2, msg.repeatedUint64.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 10);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedUint64.length);
    assertEquals(123, newMsg.repeatedUint64[0]);
    assertEquals(456, newMsg.repeatedUint64[1]);
  }

  public void testNanoRepeatedSint32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedSint32.length);
    msg.repeatedSint32 = new int[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedSint32[1]);
    assertEquals(456, msg.repeatedSint32[2]);
    msg.clear();
    assertEquals(0, msg.repeatedSint32.length);
    msg.clear()
       .repeatedSint32 = new int[] { 456 };
    assertEquals(1, msg.repeatedSint32.length);
    assertEquals(456, msg.repeatedSint32[0]);
    msg.clear();
    assertEquals(0, msg.repeatedSint32.length);

    // Test 1 entry
    msg.clear()
       .repeatedSint32 = new int[] { 123 };
    assertEquals(1, msg.repeatedSint32.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 7);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedSint32.length);
    assertEquals(123, newMsg.repeatedSint32[0]);

    // Test 2 entries
    msg.clear()
       .repeatedSint32 = new int[] { 123, 456 };
    assertEquals(2, msg.repeatedSint32.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 11);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedSint32.length);
    assertEquals(123, newMsg.repeatedSint32[0]);
    assertEquals(456, newMsg.repeatedSint32[1]);
  }

  public void testNanoRepeatedSint64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedSint64.length);
    msg.repeatedSint64 = new long[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedSint64[1]);
    assertEquals(456, msg.repeatedSint64[2]);
    msg.clear();
    assertEquals(0, msg.repeatedSint64.length);
    msg.clear()
       .repeatedSint64 = new long[] { 456 };
    assertEquals(1, msg.repeatedSint64.length);
    assertEquals(456, msg.repeatedSint64[0]);
    msg.clear();
    assertEquals(0, msg.repeatedSint64.length);

    // Test 1 entry
    msg.clear()
       .repeatedSint64 = new long[] { 123 };
    assertEquals(1, msg.repeatedSint64.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 7);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedSint64.length);
    assertEquals(123, newMsg.repeatedSint64[0]);

    // Test 2 entries
    msg.clear()
       .repeatedSint64 = new long[] { 123, 456 };
    assertEquals(2, msg.repeatedSint64.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 11);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedSint64.length);
    assertEquals(123, newMsg.repeatedSint64[0]);
    assertEquals(456, newMsg.repeatedSint64[1]);
  }

  public void testNanoRepeatedFixed32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedFixed32.length);
    msg.repeatedFixed32 = new int[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedFixed32[1]);
    assertEquals(456, msg.repeatedFixed32[2]);
    msg.clear();
    assertEquals(0, msg.repeatedFixed32.length);
    msg.clear()
       .repeatedFixed32 = new int[] { 456 };
    assertEquals(1, msg.repeatedFixed32.length);
    assertEquals(456, msg.repeatedFixed32[0]);
    msg.clear();
    assertEquals(0, msg.repeatedFixed32.length);

    // Test 1 entry
    msg.clear()
       .repeatedFixed32 = new int[] { 123 };
    assertEquals(1, msg.repeatedFixed32.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedFixed32.length);
    assertEquals(123, newMsg.repeatedFixed32[0]);

    // Test 2 entries
    msg.clear()
       .repeatedFixed32 = new int[] { 123, 456 };
    assertEquals(2, msg.repeatedFixed32.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 15);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedFixed32.length);
    assertEquals(123, newMsg.repeatedFixed32[0]);
    assertEquals(456, newMsg.repeatedFixed32[1]);
  }

  public void testNanoRepeatedFixed64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedFixed64.length);
    msg.repeatedFixed64 = new long[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedFixed64[1]);
    assertEquals(456, msg.repeatedFixed64[2]);
    msg.clear();
    assertEquals(0, msg.repeatedFixed64.length);
    msg.clear()
       .repeatedFixed64 = new long[] { 456 };
    assertEquals(1, msg.repeatedFixed64.length);
    assertEquals(456, msg.repeatedFixed64[0]);
    msg.clear();
    assertEquals(0, msg.repeatedFixed64.length);

    // Test 1 entry
    msg.clear()
       .repeatedFixed64 = new long[] { 123 };
    assertEquals(1, msg.repeatedFixed64.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 13);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedFixed64.length);
    assertEquals(123, newMsg.repeatedFixed64[0]);

    // Test 2 entries
    msg.clear()
       .repeatedFixed64 = new long[] { 123, 456 };
    assertEquals(2, msg.repeatedFixed64.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 23);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedFixed64.length);
    assertEquals(123, newMsg.repeatedFixed64[0]);
    assertEquals(456, newMsg.repeatedFixed64[1]);
  }

  public void testNanoRepeatedSfixed32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedSfixed32.length);
    msg.repeatedSfixed32 = new int[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedSfixed32[1]);
    assertEquals(456, msg.repeatedSfixed32[2]);
    msg.clear();
    assertEquals(0, msg.repeatedSfixed32.length);
    msg.clear()
       .repeatedSfixed32 = new int[] { 456 };
    assertEquals(1, msg.repeatedSfixed32.length);
    assertEquals(456, msg.repeatedSfixed32[0]);
    msg.clear();
    assertEquals(0, msg.repeatedSfixed32.length);

    // Test 1 entry
    msg.clear()
       .repeatedSfixed32 = new int[] { 123 };
    assertEquals(1, msg.repeatedSfixed32.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedSfixed32.length);
    assertEquals(123, newMsg.repeatedSfixed32[0]);

    // Test 2 entries
    msg.clear()
       .repeatedSfixed32 = new int[] { 123, 456 };
    assertEquals(2, msg.repeatedSfixed32.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 15);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedSfixed32.length);
    assertEquals(123, newMsg.repeatedSfixed32[0]);
    assertEquals(456, newMsg.repeatedSfixed32[1]);
  }

  public void testNanoRepeatedSfixed64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedSfixed64.length);
    msg.repeatedSfixed64 = new long[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedSfixed64[1]);
    assertEquals(456, msg.repeatedSfixed64[2]);
    msg.clear();
    assertEquals(0, msg.repeatedSfixed64.length);
    msg.clear()
       .repeatedSfixed64 = new long[] { 456 };
    assertEquals(1, msg.repeatedSfixed64.length);
    assertEquals(456, msg.repeatedSfixed64[0]);
    msg.clear();
    assertEquals(0, msg.repeatedSfixed64.length);

    // Test 1 entry
    msg.clear()
       .repeatedSfixed64 = new long[] { 123 };
    assertEquals(1, msg.repeatedSfixed64.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 13);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedSfixed64.length);
    assertEquals(123, newMsg.repeatedSfixed64[0]);

    // Test 2 entries
    msg.clear()
       .repeatedSfixed64 = new long[] { 123, 456 };
    assertEquals(2, msg.repeatedSfixed64.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 23);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedSfixed64.length);
    assertEquals(123, newMsg.repeatedSfixed64[0]);
    assertEquals(456, newMsg.repeatedSfixed64[1]);
  }

  public void testNanoRepeatedFloat() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedFloat.length);
    msg.repeatedFloat = new float[] { 123f, 789f, 456f };
    assertEquals(789f, msg.repeatedFloat[1]);
    assertEquals(456f, msg.repeatedFloat[2]);
    msg.clear();
    assertEquals(0, msg.repeatedFloat.length);
    msg.clear()
       .repeatedFloat = new float[] { 456f };
    assertEquals(1, msg.repeatedFloat.length);
    assertEquals(456f, msg.repeatedFloat[0]);
    msg.clear();
    assertEquals(0, msg.repeatedFloat.length);

    // Test 1 entry
    msg.clear()
       .repeatedFloat = new float[] { 123f };
    assertEquals(1, msg.repeatedFloat.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedFloat.length);
    assertEquals(123f, newMsg.repeatedFloat[0]);

    // Test 2 entries
    msg.clear()
       .repeatedFloat = new float[] { 123f, 456f };
    assertEquals(2, msg.repeatedFloat.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 15);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedFloat.length);
    assertEquals(123f, newMsg.repeatedFloat[0]);
    assertEquals(456f, newMsg.repeatedFloat[1]);
  }

  public void testNanoRepeatedDouble() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedDouble.length);
    msg.repeatedDouble = new double[] { 123.0, 789.0, 456.0 };
    assertEquals(789.0, msg.repeatedDouble[1]);
    assertEquals(456.0, msg.repeatedDouble[2]);
    msg.clear();
    assertEquals(0, msg.repeatedDouble.length);
    msg.clear()
       .repeatedDouble = new double[] { 456.0 };
    assertEquals(1, msg.repeatedDouble.length);
    assertEquals(456.0, msg.repeatedDouble[0]);
    msg.clear();
    assertEquals(0, msg.repeatedDouble.length);

    // Test 1 entry
    msg.clear()
       .repeatedDouble = new double[] { 123.0 };
    assertEquals(1, msg.repeatedDouble.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 13);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedDouble.length);
    assertEquals(123.0, newMsg.repeatedDouble[0]);

    // Test 2 entries
    msg.clear()
       .repeatedDouble = new double[] { 123.0, 456.0 };
    assertEquals(2, msg.repeatedDouble.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 23);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedDouble.length);
    assertEquals(123.0, newMsg.repeatedDouble[0]);
    assertEquals(456.0, newMsg.repeatedDouble[1]);
  }

  public void testNanoRepeatedBool() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedBool.length);
    msg.repeatedBool = new boolean[] { false, true, false };
    assertTrue(msg.repeatedBool[1]);
    assertFalse(msg.repeatedBool[2]);
    msg.clear();
    assertEquals(0, msg.repeatedBool.length);
    msg.clear()
       .repeatedBool = new boolean[] { true };
    assertEquals(1, msg.repeatedBool.length);
    assertTrue(msg.repeatedBool[0]);
    msg.clear();
    assertEquals(0, msg.repeatedBool.length);

    // Test 1 entry
    msg.clear()
       .repeatedBool = new boolean[] { false };
    assertEquals(1, msg.repeatedBool.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedBool.length);
    assertFalse(newMsg.repeatedBool[0]);

    // Test 2 entries
    msg.clear()
       .repeatedBool = new boolean[] { true, false };
    assertEquals(2, msg.repeatedBool.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedBool.length);
    assertTrue(newMsg.repeatedBool[0]);
    assertFalse(newMsg.repeatedBool[1]);
  }

  public void testNanoRepeatedString() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedString.length);
    msg.repeatedString = new String[] { "hello", "bye", "boo" };
    assertEquals("bye", msg.repeatedString[1]);
    assertEquals("boo", msg.repeatedString[2]);
    msg.clear();
    assertEquals(0, msg.repeatedString.length);
    msg.clear()
       .repeatedString = new String[] { "boo" };
    assertEquals(1, msg.repeatedString.length);
    assertEquals("boo", msg.repeatedString[0]);
    msg.clear();
    assertEquals(0, msg.repeatedString.length);

    // Test 1 entry
    msg.clear()
       .repeatedString = new String[] { "" };
    assertEquals(1, msg.repeatedString.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedString.length);
    assertTrue(newMsg.repeatedString[0].isEmpty());

    // Test 2 entries
    msg.clear()
       .repeatedString = new String[] { "hello", "world" };
    assertEquals(2, msg.repeatedString.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 19);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedString.length);
    assertEquals("hello", newMsg.repeatedString[0]);
    assertEquals("world", newMsg.repeatedString[1]);
  }

  public void testNanoRepeatedBytes() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedBytes.length);
    msg.repeatedBytes = new byte[][] {
        InternalNano.copyFromUtf8("hello"),
        InternalNano.copyFromUtf8("bye"),
        InternalNano.copyFromUtf8("boo")
    };
    assertEquals("bye", new String(msg.repeatedBytes[1], "UTF-8"));
    assertEquals("boo", new String(msg.repeatedBytes[2], "UTF-8"));
    msg.clear();
    assertEquals(0, msg.repeatedBytes.length);
    msg.clear()
       .repeatedBytes = new byte[][] { InternalNano.copyFromUtf8("boo") };
    assertEquals(1, msg.repeatedBytes.length);
    assertEquals("boo", new String(msg.repeatedBytes[0], "UTF-8"));
    msg.clear();
    assertEquals(0, msg.repeatedBytes.length);

    // Test 1 entry
    msg.clear()
       .repeatedBytes = new byte[][] { InternalNano.copyFromUtf8("") };
    assertEquals(1, msg.repeatedBytes.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedBytes.length);
    assertTrue(newMsg.repeatedBytes[0].length == 0);

    // Test 2 entries
    msg.clear()
       .repeatedBytes = new byte[][] {
      InternalNano.copyFromUtf8("hello"),
      InternalNano.copyFromUtf8("world")
    };
    assertEquals(2, msg.repeatedBytes.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 19);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedBytes.length);
    assertEquals("hello", new String(newMsg.repeatedBytes[0], "UTF-8"));
    assertEquals("world", new String(newMsg.repeatedBytes[1], "UTF-8"));
  }

  public void testNanoRepeatedGroup() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    TestAllTypesNano.RepeatedGroup group0 =
      new TestAllTypesNano.RepeatedGroup();
    group0.a = 0;
    TestAllTypesNano.RepeatedGroup group1 =
      new TestAllTypesNano.RepeatedGroup();
    group1.a = 1;
    TestAllTypesNano.RepeatedGroup group2 =
      new TestAllTypesNano.RepeatedGroup();
    group2.a = 2;

    msg.repeatedGroup = new TestAllTypesNano.RepeatedGroup[] { group0, group1, group2 };
    assertEquals(3, msg.repeatedGroup.length);
    assertEquals(0, msg.repeatedGroup[0].a);
    assertEquals(1, msg.repeatedGroup[1].a);
    assertEquals(2, msg.repeatedGroup[2].a);
    msg.clear();
    assertEquals(0, msg.repeatedGroup.length);
    msg.clear()
       .repeatedGroup = new TestAllTypesNano.RepeatedGroup[] { group1 };
    assertEquals(1, msg.repeatedGroup.length);
    assertEquals(1, msg.repeatedGroup[0].a);
    msg.clear();
    assertEquals(0, msg.repeatedGroup.length);

    // Test 1 entry
    msg.clear()
       .repeatedGroup = new TestAllTypesNano.RepeatedGroup[] { group0 };
    assertEquals(1, msg.repeatedGroup.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 7);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedGroup.length);
    assertEquals(0, newMsg.repeatedGroup[0].a);

    // Test 2 entries
    msg.clear()
       .repeatedGroup = new TestAllTypesNano.RepeatedGroup[] { group0, group1 };
    assertEquals(2, msg.repeatedGroup.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 14);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedGroup.length);
    assertEquals(0, newMsg.repeatedGroup[0].a);
    assertEquals(1, newMsg.repeatedGroup[1].a);
  }

  public void testNanoRepeatedNestedMessage() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    TestAllTypesNano.NestedMessage nestedMsg0 =
      new TestAllTypesNano.NestedMessage();
    nestedMsg0.bb = 0;
    TestAllTypesNano.NestedMessage nestedMsg1 =
      new TestAllTypesNano.NestedMessage();
    nestedMsg1.bb = 1;
    TestAllTypesNano.NestedMessage nestedMsg2 =
      new TestAllTypesNano.NestedMessage();
    nestedMsg2.bb = 2;

    msg.repeatedNestedMessage =
        new TestAllTypesNano.NestedMessage[] { nestedMsg0, nestedMsg1, nestedMsg2 };
    assertEquals(3, msg.repeatedNestedMessage.length);
    assertEquals(0, msg.repeatedNestedMessage[0].bb);
    assertEquals(1, msg.repeatedNestedMessage[1].bb);
    assertEquals(2, msg.repeatedNestedMessage[2].bb);
    msg.clear();
    assertEquals(0, msg.repeatedNestedMessage.length);
    msg.clear()
       .repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] { nestedMsg1 };
    assertEquals(1, msg.repeatedNestedMessage.length);
    assertEquals(1, msg.repeatedNestedMessage[0].bb);
    msg.clear();
    assertEquals(0, msg.repeatedNestedMessage.length);

    // Test 1 entry
    msg.clear()
       .repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] { nestedMsg0 };
    assertEquals(1, msg.repeatedNestedMessage.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedNestedMessage.length);
    assertEquals(0, newMsg.repeatedNestedMessage[0].bb);

    // Test 2 entries
    msg.clear()
       .repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] { nestedMsg0, nestedMsg1 };
    assertEquals(2, msg.repeatedNestedMessage.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 11);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedNestedMessage.length);
    assertEquals(0, newMsg.repeatedNestedMessage[0].bb);
    assertEquals(1, newMsg.repeatedNestedMessage[1].bb);
  }

  public void testNanoRepeatedForeignMessage() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    NanoOuterClass.ForeignMessageNano foreignMsg0 =
      new NanoOuterClass.ForeignMessageNano();
    foreignMsg0.c = 0;
    NanoOuterClass.ForeignMessageNano foreignMsg1 =
      new NanoOuterClass.ForeignMessageNano();
    foreignMsg1.c = 1;
    NanoOuterClass.ForeignMessageNano foreignMsg2 =
      new NanoOuterClass.ForeignMessageNano();
    foreignMsg2.c = 2;

    msg.repeatedForeignMessage =
        new NanoOuterClass.ForeignMessageNano[] { foreignMsg0, foreignMsg1, foreignMsg2 };
    assertEquals(3, msg.repeatedForeignMessage.length);
    assertEquals(0, msg.repeatedForeignMessage[0].c);
    assertEquals(1, msg.repeatedForeignMessage[1].c);
    assertEquals(2, msg.repeatedForeignMessage[2].c);
    msg.clear();
    assertEquals(0, msg.repeatedForeignMessage.length);
    msg.clear()
       .repeatedForeignMessage = new NanoOuterClass.ForeignMessageNano[] { foreignMsg1 };
    assertEquals(1, msg.repeatedForeignMessage.length);
    assertEquals(1, msg.repeatedForeignMessage[0].c);
    msg.clear();
    assertEquals(0, msg.repeatedForeignMessage.length);

    // Test 1 entry
    msg.clear()
       .repeatedForeignMessage = new NanoOuterClass.ForeignMessageNano[] { foreignMsg0 };
    assertEquals(1, msg.repeatedForeignMessage.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedForeignMessage.length);
    assertEquals(0, newMsg.repeatedForeignMessage[0].c);

    // Test 2 entries
    msg.clear()
       .repeatedForeignMessage = new NanoOuterClass.ForeignMessageNano[] { foreignMsg0, foreignMsg1 };
    assertEquals(2, msg.repeatedForeignMessage.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 11);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedForeignMessage.length);
    assertEquals(0, newMsg.repeatedForeignMessage[0].c);
    assertEquals(1, newMsg.repeatedForeignMessage[1].c);
  }

  public void testNanoRepeatedImportMessage() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    UnittestImportNano.ImportMessageNano foreignMsg0 =
      new UnittestImportNano.ImportMessageNano();
    foreignMsg0.d = 0;
    UnittestImportNano.ImportMessageNano foreignMsg1 =
      new UnittestImportNano.ImportMessageNano();
    foreignMsg1.d = 1;
    UnittestImportNano.ImportMessageNano foreignMsg2 =
      new UnittestImportNano.ImportMessageNano();
    foreignMsg2.d = 2;

    msg.repeatedImportMessage =
        new UnittestImportNano.ImportMessageNano[] { foreignMsg0, foreignMsg1, foreignMsg2 };
    assertEquals(3, msg.repeatedImportMessage.length);
    assertEquals(0, msg.repeatedImportMessage[0].d);
    assertEquals(1, msg.repeatedImportMessage[1].d);
    assertEquals(2, msg.repeatedImportMessage[2].d);
    msg.clear();
    assertEquals(0, msg.repeatedImportMessage.length);
    msg.clear()
       .repeatedImportMessage = new UnittestImportNano.ImportMessageNano[] { foreignMsg1 };
    assertEquals(1, msg.repeatedImportMessage.length);
    assertEquals(1, msg.repeatedImportMessage[0].d);
    msg.clear();
    assertEquals(0, msg.repeatedImportMessage.length);

    // Test 1 entry
    msg.clear()
       .repeatedImportMessage = new UnittestImportNano.ImportMessageNano[] { foreignMsg0 };
    assertEquals(1, msg.repeatedImportMessage.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedImportMessage.length);
    assertEquals(0, newMsg.repeatedImportMessage[0].d);

    // Test 2 entries
    msg.clear()
       .repeatedImportMessage = new UnittestImportNano.ImportMessageNano[] { foreignMsg0, foreignMsg1 };
    assertEquals(2, msg.repeatedImportMessage.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 11);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedImportMessage.length);
    assertEquals(0, newMsg.repeatedImportMessage[0].d);
    assertEquals(1, newMsg.repeatedImportMessage[1].d);
  }

  public void testNanoRepeatedNestedEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedNestedEnum = new int[] {
        TestAllTypesNano.FOO,
        TestAllTypesNano.BAR,
        TestAllTypesNano.BAZ
    };
    assertEquals(3, msg.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, msg.repeatedNestedEnum[0]);
    assertEquals(TestAllTypesNano.BAR, msg.repeatedNestedEnum[1]);
    assertEquals(TestAllTypesNano.BAZ, msg.repeatedNestedEnum[2]);
    msg.clear();
    assertEquals(0, msg.repeatedNestedEnum.length);
    msg.clear()
       .repeatedNestedEnum = new int[] { TestAllTypesNano.BAR };
    assertEquals(1, msg.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.BAR, msg.repeatedNestedEnum[0]);
    msg.clear();
    assertEquals(0, msg.repeatedNestedEnum.length);

    // Test 1 entry
    msg.clear()
       .repeatedNestedEnum = new int[] { TestAllTypesNano.FOO };
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, msg.repeatedNestedEnum[0]);

    // Test 2 entries
    msg.clear()
       .repeatedNestedEnum = new int[] { TestAllTypesNano.FOO, TestAllTypesNano.BAR };
    assertEquals(2, msg.repeatedNestedEnum.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, msg.repeatedNestedEnum[0]);
    assertEquals(TestAllTypesNano.BAR, msg.repeatedNestedEnum[1]);
  }

  public void testNanoRepeatedForeignEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedForeignEnum = new int[] {
        NanoOuterClass.FOREIGN_NANO_FOO,
        NanoOuterClass.FOREIGN_NANO_BAR,
        NanoOuterClass.FOREIGN_NANO_BAZ
    };
    assertEquals(3, msg.repeatedForeignEnum.length);
    assertEquals(NanoOuterClass.FOREIGN_NANO_FOO, msg.repeatedForeignEnum[0]);
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAR, msg.repeatedForeignEnum[1]);
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAZ, msg.repeatedForeignEnum[2]);
    msg.clear();
    assertEquals(0, msg.repeatedForeignEnum.length);
    msg.clear()
       .repeatedForeignEnum = new int[] { NanoOuterClass.FOREIGN_NANO_BAR };
    assertEquals(1, msg.repeatedForeignEnum.length);
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAR, msg.repeatedForeignEnum[0]);
    msg.clear();
    assertEquals(0, msg.repeatedForeignEnum.length);

    // Test 1 entry
    msg.clear()
       .repeatedForeignEnum = new int[] { NanoOuterClass.FOREIGN_NANO_FOO };
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedForeignEnum.length);
    assertEquals(NanoOuterClass.FOREIGN_NANO_FOO, msg.repeatedForeignEnum[0]);

    // Test 2 entries
    msg.clear()
       .repeatedForeignEnum = new int[] {
      NanoOuterClass.FOREIGN_NANO_FOO,
      NanoOuterClass.FOREIGN_NANO_BAR
    };
    assertEquals(2, msg.repeatedForeignEnum.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedForeignEnum.length);
    assertEquals(NanoOuterClass.FOREIGN_NANO_FOO, msg.repeatedForeignEnum[0]);
    assertEquals(NanoOuterClass.FOREIGN_NANO_BAR, msg.repeatedForeignEnum[1]);
  }

  public void testNanoRepeatedImportEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedImportEnum = new int[] {
        UnittestImportNano.IMPORT_NANO_FOO,
        UnittestImportNano.IMPORT_NANO_BAR,
        UnittestImportNano.IMPORT_NANO_BAZ
    };
    assertEquals(3, msg.repeatedImportEnum.length);
    assertEquals(UnittestImportNano.IMPORT_NANO_FOO, msg.repeatedImportEnum[0]);
    assertEquals(UnittestImportNano.IMPORT_NANO_BAR, msg.repeatedImportEnum[1]);
    assertEquals(UnittestImportNano.IMPORT_NANO_BAZ, msg.repeatedImportEnum[2]);
    msg.clear();
    assertEquals(0, msg.repeatedImportEnum.length);
    msg.clear()
       .repeatedImportEnum = new int[] { UnittestImportNano.IMPORT_NANO_BAR };
    assertEquals(1, msg.repeatedImportEnum.length);
    assertEquals(UnittestImportNano.IMPORT_NANO_BAR, msg.repeatedImportEnum[0]);
    msg.clear();
    assertEquals(0, msg.repeatedImportEnum.length);

    // Test 1 entry
    msg.clear()
       .repeatedImportEnum = new int[] { UnittestImportNano.IMPORT_NANO_FOO };
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedImportEnum.length);
    assertEquals(UnittestImportNano.IMPORT_NANO_FOO, msg.repeatedImportEnum[0]);

    // Test 2 entries
    msg.clear()
       .repeatedImportEnum = new int[] {
      UnittestImportNano.IMPORT_NANO_FOO,
      UnittestImportNano.IMPORT_NANO_BAR
    };
    assertEquals(2, msg.repeatedImportEnum.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedImportEnum.length);
    assertEquals(UnittestImportNano.IMPORT_NANO_FOO, msg.repeatedImportEnum[0]);
    assertEquals(UnittestImportNano.IMPORT_NANO_BAR, msg.repeatedImportEnum[1]);
  }

  public void testNanoRepeatedStringPiece() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedStringPiece.length);
    msg.repeatedStringPiece = new String[] { "hello", "bye", "boo" };
    assertEquals("bye", msg.repeatedStringPiece[1]);
    assertEquals("boo", msg.repeatedStringPiece[2]);
    msg.clear();
    assertEquals(0, msg.repeatedStringPiece.length);
    msg.clear()
       .repeatedStringPiece = new String[] { "boo" };
    assertEquals(1, msg.repeatedStringPiece.length);
    assertEquals("boo", msg.repeatedStringPiece[0]);
    msg.clear();
    assertEquals(0, msg.repeatedStringPiece.length);

    // Test 1 entry
    msg.clear()
       .repeatedStringPiece = new String[] { "" };
    assertEquals(1, msg.repeatedStringPiece.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedStringPiece.length);
    assertTrue(newMsg.repeatedStringPiece[0].isEmpty());

    // Test 2 entries
    msg.clear()
       .repeatedStringPiece = new String[] { "hello", "world" };
    assertEquals(2, msg.repeatedStringPiece.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 19);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedStringPiece.length);
    assertEquals("hello", newMsg.repeatedStringPiece[0]);
    assertEquals("world", newMsg.repeatedStringPiece[1]);
  }

  public void testNanoRepeatedCord() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedCord.length);
    msg.repeatedCord = new String[] { "hello", "bye", "boo" };
    assertEquals("bye", msg.repeatedCord[1]);
    assertEquals("boo", msg.repeatedCord[2]);
    msg.clear();
    assertEquals(0, msg.repeatedCord.length);
    msg.clear()
       .repeatedCord = new String[] { "boo" };
    assertEquals(1, msg.repeatedCord.length);
    assertEquals("boo", msg.repeatedCord[0]);
    msg.clear();
    assertEquals(0, msg.repeatedCord.length);

    // Test 1 entry
    msg.clear()
       .repeatedCord = new String[] { "" };
    assertEquals(1, msg.repeatedCord.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 6);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedCord.length);
    assertTrue(newMsg.repeatedCord[0].isEmpty());

    // Test 2 entries
    msg.clear()
       .repeatedCord = new String[] { "hello", "world" };
    assertEquals(2, msg.repeatedCord.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 19);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedCord.length);
    assertEquals("hello", newMsg.repeatedCord[0]);
    assertEquals("world", newMsg.repeatedCord[1]);
  }

  public void testNanoRepeatedPackedInt32() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedPackedInt32.length);
    msg.repeatedPackedInt32 = new int[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedPackedInt32[1]);
    assertEquals(456, msg.repeatedPackedInt32[2]);
    msg.clear();
    assertEquals(0, msg.repeatedPackedInt32.length);
    msg.clear()
       .repeatedPackedInt32 = new int[] { 456 };
    assertEquals(1, msg.repeatedPackedInt32.length);
    assertEquals(456, msg.repeatedPackedInt32[0]);
    msg.clear();
    assertEquals(0, msg.repeatedPackedInt32.length);

    // Test 1 entry
    msg.clear()
       .repeatedPackedInt32 = new int[] { 123 };
    assertEquals(1, msg.repeatedPackedInt32.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 7);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedPackedInt32.length);
    assertEquals(123, newMsg.repeatedPackedInt32[0]);

    // Test 2 entries
    msg.clear()
       .repeatedPackedInt32 = new int[] { 123, 456 };
    assertEquals(2, msg.repeatedPackedInt32.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 9);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedPackedInt32.length);
    assertEquals(123, newMsg.repeatedPackedInt32[0]);
    assertEquals(456, newMsg.repeatedPackedInt32[1]);
  }

  public void testNanoRepeatedPackedSfixed64() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    assertEquals(0, msg.repeatedPackedSfixed64.length);
    msg.repeatedPackedSfixed64 = new long[] { 123, 789, 456 };
    assertEquals(789, msg.repeatedPackedSfixed64[1]);
    assertEquals(456, msg.repeatedPackedSfixed64[2]);
    msg.clear();
    assertEquals(0, msg.repeatedPackedSfixed64.length);
    msg.clear()
       .repeatedPackedSfixed64 = new long[] { 456 };
    assertEquals(1, msg.repeatedPackedSfixed64.length);
    assertEquals(456, msg.repeatedPackedSfixed64[0]);
    msg.clear();
    assertEquals(0, msg.repeatedPackedSfixed64.length);

    // Test 1 entry
    msg.clear()
       .repeatedPackedSfixed64 = new long[] { 123 };
    assertEquals(1, msg.repeatedPackedSfixed64.length);
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 14);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedPackedSfixed64.length);
    assertEquals(123, newMsg.repeatedPackedSfixed64[0]);

    // Test 2 entries
    msg.clear()
       .repeatedPackedSfixed64 = new long[] { 123, 456 };
    assertEquals(2, msg.repeatedPackedSfixed64.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 22);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedPackedSfixed64.length);
    assertEquals(123, newMsg.repeatedPackedSfixed64[0]);
    assertEquals(456, newMsg.repeatedPackedSfixed64[1]);
  }

  public void testNanoRepeatedPackedNestedEnum() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedPackedNestedEnum = new int[] {
        TestAllTypesNano.FOO,
        TestAllTypesNano.BAR,
        TestAllTypesNano.BAZ
    };
    assertEquals(3, msg.repeatedPackedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, msg.repeatedPackedNestedEnum[0]);
    assertEquals(TestAllTypesNano.BAR, msg.repeatedPackedNestedEnum[1]);
    assertEquals(TestAllTypesNano.BAZ, msg.repeatedPackedNestedEnum[2]);
    msg.clear();
    assertEquals(0, msg.repeatedPackedNestedEnum.length);
    msg.clear()
       .repeatedPackedNestedEnum = new int[] { TestAllTypesNano.BAR };
    assertEquals(1, msg.repeatedPackedNestedEnum.length);
    assertEquals(TestAllTypesNano.BAR, msg.repeatedPackedNestedEnum[0]);
    msg.clear();
    assertEquals(0, msg.repeatedPackedNestedEnum.length);

    // Test 1 entry
    msg.clear()
       .repeatedPackedNestedEnum = new int[] { TestAllTypesNano.FOO };
    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 7);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(1, newMsg.repeatedPackedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, msg.repeatedPackedNestedEnum[0]);

    // Test 2 entries
    msg.clear()
       .repeatedPackedNestedEnum = new int[] { TestAllTypesNano.FOO, TestAllTypesNano.BAR };
    assertEquals(2, msg.repeatedPackedNestedEnum.length);
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 8);
    assertEquals(result.length, msgSerializedSize);

    newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(2, newMsg.repeatedPackedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, msg.repeatedPackedNestedEnum[0]);
    assertEquals(TestAllTypesNano.BAR, msg.repeatedPackedNestedEnum[1]);
  }

  public void testNanoRepeatedPackedSerializedSize() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedPackedInt32 = new int[] { 123, 789, 456 };
    int msgSerializedSize = msg.getSerializedSize();
    byte [] result = MessageNano.toByteArray(msg);
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 11);
    assertEquals(result.length, msgSerializedSize);
    TestAllTypesNano msg2 = new TestAllTypesNano();
    msg2.repeatedPackedInt32 = new int[] { 123, 789, 456 };
    byte [] result2 = new byte[msgSerializedSize];
    MessageNano.toByteArray(msg2, result2, 0, msgSerializedSize);

    // Check equal size and content.
    assertEquals(msgSerializedSize, msg2.getSerializedSize());
    assertTrue(Arrays.equals(result, result2));
  }

  public void testNanoRepeatedInt32ReMerge() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedInt32 = new int[] { 234 };
    byte [] result1 = MessageNano.toByteArray(msg);

    msg.clear().optionalInt32 = 789;
    byte [] result2 = MessageNano.toByteArray(msg);

    msg.clear().repeatedInt32 = new int[] { 123, 456 };
    byte [] result3 = MessageNano.toByteArray(msg);

    // Concatenate the three serializations and read as one message.
    byte [] result = new byte[result1.length + result2.length + result3.length];
    System.arraycopy(result1, 0, result, 0, result1.length);
    System.arraycopy(result2, 0, result, result1.length, result2.length);
    System.arraycopy(result3, 0, result, result1.length + result2.length, result3.length);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(789, newMsg.optionalInt32);
    assertEquals(3, newMsg.repeatedInt32.length);
    assertEquals(234, newMsg.repeatedInt32[0]);
    assertEquals(123, newMsg.repeatedInt32[1]);
    assertEquals(456, newMsg.repeatedInt32[2]);
  }

  public void testNanoRepeatedNestedEnumReMerge() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.repeatedNestedEnum = new int[] { TestAllTypesNano.FOO };
    byte [] result1 = MessageNano.toByteArray(msg);

    msg.clear().optionalInt32 = 789;
    byte [] result2 = MessageNano.toByteArray(msg);

    msg.clear().repeatedNestedEnum = new int[] { TestAllTypesNano.BAR, TestAllTypesNano.FOO };
    byte [] result3 = MessageNano.toByteArray(msg);

    // Concatenate the three serializations and read as one message.
    byte [] result = new byte[result1.length + result2.length + result3.length];
    System.arraycopy(result1, 0, result, 0, result1.length);
    System.arraycopy(result2, 0, result, result1.length, result2.length);
    System.arraycopy(result3, 0, result, result1.length + result2.length, result3.length);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(789, newMsg.optionalInt32);
    assertEquals(3, newMsg.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, newMsg.repeatedNestedEnum[0]);
    assertEquals(TestAllTypesNano.BAR, newMsg.repeatedNestedEnum[1]);
    assertEquals(TestAllTypesNano.FOO, newMsg.repeatedNestedEnum[2]);
  }

  public void testNanoRepeatedNestedMessageReMerge() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    TestAllTypesNano.NestedMessage nestedMsg0 =
      new TestAllTypesNano.NestedMessage();
    nestedMsg0.bb = 0;
    TestAllTypesNano.NestedMessage nestedMsg1 =
      new TestAllTypesNano.NestedMessage();
    nestedMsg1.bb = 1;
    TestAllTypesNano.NestedMessage nestedMsg2 =
      new TestAllTypesNano.NestedMessage();
    nestedMsg2.bb = 2;

    msg.repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] { nestedMsg0 };
    byte [] result1 = MessageNano.toByteArray(msg);

    msg.clear().optionalInt32 = 789;
    byte [] result2 = MessageNano.toByteArray(msg);

    msg.clear().repeatedNestedMessage =
        new TestAllTypesNano.NestedMessage[] { nestedMsg1, nestedMsg2 };
    byte [] result3 = MessageNano.toByteArray(msg);

    // Concatenate the three serializations and read as one message.
    byte [] result = new byte[result1.length + result2.length + result3.length];
    System.arraycopy(result1, 0, result, 0, result1.length);
    System.arraycopy(result2, 0, result, result1.length, result2.length);
    System.arraycopy(result3, 0, result, result1.length + result2.length, result3.length);

    TestAllTypesNano newMsg = TestAllTypesNano.parseFrom(result);
    assertEquals(789, newMsg.optionalInt32);
    assertEquals(3, newMsg.repeatedNestedMessage.length);
    assertEquals(nestedMsg0.bb, newMsg.repeatedNestedMessage[0].bb);
    assertEquals(nestedMsg1.bb, newMsg.repeatedNestedMessage[1].bb);
    assertEquals(nestedMsg2.bb, newMsg.repeatedNestedMessage[2].bb);
  }

  /**
   * Tests that invalid enum values from the wire are not accepted.
   */
  public void testNanoEnumValidity() throws Exception {
    final int invalid = 120;
    final int alsoInvalid = 121;

    EnumValidity.M m = new EnumValidity.M();
    // Sanity check & baseline of the assertions for the first case below.
    assertEquals(EnumValidity.E.default_, m.optionalE);
    assertEquals(EnumValidity.E.BAZ, m.defaultE);

    m.optionalE = invalid;
    m.defaultE = invalid;
    // E contains all valid values
    m.repeatedE = new int[] {EnumValidity.E.FOO, EnumValidity.E.BAR};
    m.packedE = new int[] {EnumValidity.E.FOO, EnumValidity.E.BAZ};
    // E2 contains some invalid values
    m.repeatedE2 = new int[] {invalid, EnumValidity.E.BAR, alsoInvalid};
    m.packedE2 = new int[] {EnumValidity.E.FOO, invalid, alsoInvalid};
    // E3 contains all invalid values
    m.repeatedE3 = new int[] {invalid, invalid};
    m.packedE3 = new int[] {alsoInvalid, alsoInvalid};
    byte[] serialized = MessageNano.toByteArray(m);
    // Sanity check that we do have all data in the byte array.
    assertEquals(31, serialized.length);

    // Test 1: tests that invalid values aren't included in the deserialized message.
    EnumValidity.M deserialized = MessageNano.mergeFrom(new EnumValidity.M(), serialized);
    assertEquals(EnumValidity.E.default_, deserialized.optionalE);
    assertEquals(EnumValidity.E.BAZ, deserialized.defaultE);
    assertTrue(Arrays.equals(
        new int[] {EnumValidity.E.FOO, EnumValidity.E.BAR}, deserialized.repeatedE));
    assertTrue(Arrays.equals(
        new int[] {EnumValidity.E.FOO, EnumValidity.E.BAZ}, deserialized.packedE));
    assertTrue(Arrays.equals(
        new int[] {EnumValidity.E.BAR}, deserialized.repeatedE2));
    assertTrue(Arrays.equals(
        new int[] {EnumValidity.E.FOO}, deserialized.packedE2));
    assertEquals(0, deserialized.repeatedE3.length);
    assertEquals(0, deserialized.packedE3.length);

    // Test 2: tests that invalid values do not override previous values in the field, including
    // arrays, including pre-existing invalid values.
    deserialized.optionalE = EnumValidity.E.BAR;
    deserialized.defaultE = alsoInvalid;
    deserialized.repeatedE = new int[] {EnumValidity.E.BAZ};
    deserialized.packedE = new int[] {EnumValidity.E.BAZ, alsoInvalid};
    deserialized.repeatedE2 = new int[] {invalid, alsoInvalid};
    deserialized.packedE2 = null;
    deserialized.repeatedE3 = null;
    deserialized.packedE3 = new int[0];
    MessageNano.mergeFrom(deserialized, serialized);
    assertEquals(EnumValidity.E.BAR, deserialized.optionalE);
    assertEquals(alsoInvalid, deserialized.defaultE);
    assertTrue(Arrays.equals(
        new int[] {EnumValidity.E.BAZ, /* + */ EnumValidity.E.FOO, EnumValidity.E.BAR},
        deserialized.repeatedE));
    assertTrue(Arrays.equals(
        new int[] {EnumValidity.E.BAZ, alsoInvalid, /* + */ EnumValidity.E.FOO, EnumValidity.E.BAZ},
        deserialized.packedE));
    assertTrue(Arrays.equals(
        new int[] {invalid, alsoInvalid, /* + */ EnumValidity.E.BAR},
        deserialized.repeatedE2));
    assertTrue(Arrays.equals(
        new int[] {/* <null> + */ EnumValidity.E.FOO},
        deserialized.packedE2));
    assertNull(deserialized.repeatedE3); // null + all invalid == null
    assertEquals(0, deserialized.packedE3.length); // empty + all invalid == empty

    // Test 3: reading by alternative forms
    EnumValidity.Alt alt = MessageNano.mergeFrom(new EnumValidity.Alt(), serialized);
    assertEquals(EnumValidity.E.BAR, // last valid value in m.repeatedE2
        alt.repeatedE2AsOptional);
    assertTrue(Arrays.equals(new int[] {EnumValidity.E.FOO}, alt.packedE2AsNonPacked));
    assertEquals(0, alt.nonPackedE3AsPacked.length);
  }

  /**
   * Tests the same as {@link #testNanoEnumValidity()} with accessor style. Repeated fields are
   * not re-tested here because they are not affected by the accessor style.
   */
  public void testNanoEnumValidityAccessors() throws Exception {
    final int invalid = 120;
    final int alsoInvalid = 121;

    EnumValidityAccessors.M m = new EnumValidityAccessors.M();
    // Sanity check & baseline of the assertions for the first case below.
    assertEquals(EnumValidityAccessors.default_, m.getOptionalE());
    assertEquals(EnumValidityAccessors.BAZ, m.getDefaultE());

    m.setOptionalE(invalid);
    m.setDefaultE(invalid);
    // Set repeatedE2 for Alt.repeatedE2AsOptional
    m.repeatedE2 = new int[] {invalid, EnumValidityAccessors.BAR, alsoInvalid};
    byte[] serialized = MessageNano.toByteArray(m);
    // Sanity check that we do have all data in the byte array.
    assertEquals(10, serialized.length);

    // Test 1: tests that invalid values aren't included in the deserialized message.
    EnumValidityAccessors.M deserialized =
        MessageNano.mergeFrom(new EnumValidityAccessors.M(), serialized);
    assertEquals(EnumValidityAccessors.default_, deserialized.getOptionalE());
    assertEquals(EnumValidityAccessors.BAZ, deserialized.getDefaultE());

    // Test 2: tests that invalid values do not override previous values in the field, including
    // pre-existing invalid values.
    deserialized.setOptionalE(EnumValidityAccessors.BAR);
    deserialized.setDefaultE(alsoInvalid);
    MessageNano.mergeFrom(deserialized, serialized);
    assertEquals(EnumValidityAccessors.BAR, deserialized.getOptionalE());
    assertEquals(alsoInvalid, deserialized.getDefaultE());

    // Test 3: reading by alternative forms
    EnumValidityAccessors.Alt alt =
        MessageNano.mergeFrom(new EnumValidityAccessors.Alt(), serialized);
    assertEquals(EnumValidityAccessors.BAR, // last valid value in m.repeatedE2
        alt.getRepeatedE2AsOptional());
  }

  /**
   * Tests that code generation correctly wraps a single message into its outer
   * class. The class {@code SingleMessageNano} is imported from the outer
   * class {@code UnittestSingleNano}, whose name is implicit. Any error would
   * cause this method to fail compilation.
   */
  public void testNanoSingle() throws Exception {
    SingleMessageNano msg = new SingleMessageNano();
  }

  /**
   * Tests that code generation correctly skips generating the outer class if
   * unnecessary, letting a file-scope entity have the same name. The class
   * {@code MultipleNameClashNano} shares the same name with the file's outer
   * class defined explicitly, but the file contains no other entities and has
   * java_multiple_files set. Any error would cause this method to fail
   * compilation.
   */
  public void testNanoMultipleNameClash() throws Exception {
    MultipleNameClashNano msg = new MultipleNameClashNano();
    msg.field = 0;
  }

  /**
   * Tests that code generation correctly handles enums in different scopes in
   * a source file with the option java_multiple_files set to true. Any error
   * would cause this method to fail compilation.
   */
  public void testNanoMultipleEnumScoping() throws Exception {
    FileScopeEnumRefNano msg1 = new FileScopeEnumRefNano();
    msg1.enumField = UnittestMultipleNano.ONE;
    MessageScopeEnumRefNano msg2 = new MessageScopeEnumRefNano();
    msg2.enumField = MessageScopeEnumRefNano.TWO;
  }

  /**
   * Tests that code generation with mixed values of the java_multiple_files
   * options between the main source file and the imported source files would
   * generate correct references. Any error would cause this method to fail
   * compilation.
   */
  public void testNanoMultipleImportingNonMultiple() throws Exception {
    UnittestImportNano.ImportMessageNano importMsg = new UnittestImportNano.ImportMessageNano();
    MultipleImportingNonMultipleNano1 nano1 = new MultipleImportingNonMultipleNano1();
    nano1.field = importMsg;
    MultipleImportingNonMultipleNano2 nano2 = new MultipleImportingNonMultipleNano2();
    nano2.nano1 = nano1;
  }

  public void testNanoDefaults() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    for (int i = 0; i < 2; i++) {
      assertEquals(41, msg.defaultInt32);
      assertEquals(42, msg.defaultInt64);
      assertEquals(43, msg.defaultUint32);
      assertEquals(44, msg.defaultUint64);
      assertEquals(-45, msg.defaultSint32);
      assertEquals(46, msg.defaultSint64);
      assertEquals(47, msg.defaultFixed32);
      assertEquals(48, msg.defaultFixed64);
      assertEquals(49, msg.defaultSfixed32);
      assertEquals(-50, msg.defaultSfixed64);
      assertTrue(51.5f == msg.defaultFloat);
      assertTrue(52.0e3 == msg.defaultDouble);
      assertEquals(true, msg.defaultBool);
      assertEquals("hello", msg.defaultString);
      assertEquals("world", new String(msg.defaultBytes, "UTF-8"));
      assertEquals("dünya", msg.defaultStringNonascii);
      assertEquals("dünyab", new String(msg.defaultBytesNonascii, "UTF-8"));
      assertEquals(TestAllTypesNano.BAR, msg.defaultNestedEnum);
      assertEquals(NanoOuterClass.FOREIGN_NANO_BAR, msg.defaultForeignEnum);
      assertEquals(UnittestImportNano.IMPORT_NANO_BAR, msg.defaultImportEnum);
      assertEquals(Float.POSITIVE_INFINITY, msg.defaultFloatInf);
      assertEquals(Float.NEGATIVE_INFINITY, msg.defaultFloatNegInf);
      assertEquals(Float.NaN, msg.defaultFloatNan);
      assertEquals(Double.POSITIVE_INFINITY, msg.defaultDoubleInf);
      assertEquals(Double.NEGATIVE_INFINITY, msg.defaultDoubleNegInf);
      assertEquals(Double.NaN, msg.defaultDoubleNan);

      // Default values are not output, except for required fields.
      byte [] result = MessageNano.toByteArray(msg);
      int msgSerializedSize = msg.getSerializedSize();
      //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
      assertTrue(msgSerializedSize == 3);
      assertEquals(result.length, msgSerializedSize);
      msg.clear();
    }
  }

  public void testNanoWithHasParseFrom() throws Exception {
    TestAllTypesNanoHas msg = null;
    // Test false on creation, after clear and upon empty parse.
    for (int i = 0; i < 3; i++) {
      if (i == 0) {
        msg = new TestAllTypesNanoHas();
      } else if (i == 1) {
        msg.clear();
      } else if (i == 2) {
        msg = TestAllTypesNanoHas.parseFrom(new byte[0]);
      }
      assertFalse(msg.hasOptionalInt32);
      assertFalse(msg.hasOptionalString);
      assertFalse(msg.hasOptionalBytes);
      assertFalse(msg.hasOptionalNestedEnum);
      assertFalse(msg.hasDefaultInt32);
      assertFalse(msg.hasDefaultString);
      assertFalse(msg.hasDefaultBytes);
      assertFalse(msg.hasDefaultFloatNan);
      assertFalse(msg.hasDefaultNestedEnum);
      assertFalse(msg.hasId);
      assertFalse(msg.hasRequiredEnum);
      msg.optionalInt32 = 123;
      msg.optionalNestedMessage = new TestAllTypesNanoHas.NestedMessage();
      msg.optionalNestedMessage.bb = 2;
      msg.optionalNestedEnum = TestAllTypesNano.BAZ;
    }

    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 10);
    assertEquals(result.length, msgSerializedSize);

    // Has fields true upon parse.
    TestAllTypesNanoHas newMsg = TestAllTypesNanoHas.parseFrom(result);
    assertEquals(123, newMsg.optionalInt32);
    assertTrue(newMsg.hasOptionalInt32);
    assertEquals(2, newMsg.optionalNestedMessage.bb);
    assertTrue(newMsg.optionalNestedMessage.hasBb);
    assertEquals(TestAllTypesNanoHas.BAZ, newMsg.optionalNestedEnum);
    assertTrue(newMsg.hasOptionalNestedEnum);
  }

  public void testNanoWithHasSerialize() throws Exception {
    TestAllTypesNanoHas msg = new TestAllTypesNanoHas();
    msg.hasOptionalInt32 = true;
    msg.hasOptionalString = true;
    msg.hasOptionalBytes = true;
    msg.optionalNestedMessage = new TestAllTypesNanoHas.NestedMessage();
    msg.optionalNestedMessage.hasBb = true;
    msg.hasOptionalNestedEnum = true;
    msg.hasDefaultInt32 = true;
    msg.hasDefaultString = true;
    msg.hasDefaultBytes = true;
    msg.hasDefaultFloatNan = true;
    msg.hasDefaultNestedEnum = true;
    msg.hasId = true;
    msg.hasRequiredEnum = true;

    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    assertEquals(result.length, msgSerializedSize);

    // Now deserialize and find that all fields are set and equal to their defaults.
    TestAllTypesNanoHas newMsg = TestAllTypesNanoHas.parseFrom(result);
    assertTrue(newMsg.hasOptionalInt32);
    assertTrue(newMsg.hasOptionalString);
    assertTrue(newMsg.hasOptionalBytes);
    assertTrue(newMsg.optionalNestedMessage.hasBb);
    assertTrue(newMsg.hasOptionalNestedEnum);
    assertTrue(newMsg.hasDefaultInt32);
    assertTrue(newMsg.hasDefaultString);
    assertTrue(newMsg.hasDefaultBytes);
    assertTrue(newMsg.hasDefaultFloatNan);
    assertTrue(newMsg.hasDefaultNestedEnum);
    assertTrue(newMsg.hasId);
    assertTrue(newMsg.hasRequiredEnum);
    assertEquals(0, newMsg.optionalInt32);
    assertEquals(0, newMsg.optionalString.length());
    assertEquals(0, newMsg.optionalBytes.length);
    assertEquals(0, newMsg.optionalNestedMessage.bb);
    assertEquals(TestAllTypesNanoHas.FOO, newMsg.optionalNestedEnum);
    assertEquals(41, newMsg.defaultInt32);
    assertEquals("hello", newMsg.defaultString);
    assertEquals("world", new String(newMsg.defaultBytes, "UTF-8"));
    assertEquals(TestAllTypesNanoHas.BAR, newMsg.defaultNestedEnum);
    assertEquals(Float.NaN, newMsg.defaultFloatNan);
    assertEquals(0, newMsg.id);
    assertEquals(TestAllTypesNanoHas.FOO, newMsg.requiredEnum);
  }

  public void testNanoWithAccessorsBasic() throws Exception {
    TestNanoAccessors msg = new TestNanoAccessors();

    // Makes sure required, repeated, and message fields are still public
    msg.id = 3;
    msg.repeatedBytes = new byte[2][3];
    msg.optionalNestedMessage = null;

    // Test accessors
    assertEquals(0, msg.getOptionalInt32());
    assertFalse(msg.hasOptionalInt32());
    msg.setOptionalInt32(135);
    assertEquals(135, msg.getOptionalInt32());
    assertTrue(msg.hasOptionalInt32());
    msg.clearOptionalInt32();
    assertFalse(msg.hasOptionalInt32());
    msg.setOptionalInt32(0); // default value
    assertTrue(msg.hasOptionalInt32());

    // Test NPE
    try {
      msg.setOptionalBytes(null);
      fail();
    } catch (NullPointerException expected) {}
    try {
      msg.setOptionalString(null);
      fail();
    } catch (NullPointerException expected) {}

    // Test has bit on bytes field with defaults and clear() re-clones the default array
    assertFalse(msg.hasDefaultBytes());
    byte[] defaultBytes = msg.getDefaultBytes();
    msg.setDefaultBytes(defaultBytes);
    assertTrue(msg.hasDefaultBytes());
    msg.clearDefaultBytes();
    assertFalse(msg.hasDefaultBytes());
    defaultBytes[0]++; // modify original array
    assertFalse(Arrays.equals(defaultBytes, msg.getDefaultBytes()));

    // Test has bits that require additional bit fields
    assertFalse(msg.hasBitFieldCheck());
    msg.setBitFieldCheck(0);
    assertTrue(msg.hasBitFieldCheck());
    assertFalse(msg.hasBeforeBitFieldCheck()); // checks bit field does not leak
    assertFalse(msg.hasAfterBitFieldCheck());

    // Test clear() clears has bits
    msg.setOptionalString("hi");
    msg.setDefaultString("there");
    msg.clear();
    assertFalse(msg.hasOptionalString());
    assertFalse(msg.hasDefaultString());
    assertFalse(msg.hasBitFieldCheck());

    // Test set() and clear() returns itself (compiles = success)
    msg.clear()
        .setOptionalInt32(3)
        .clearDefaultBytes()
        .setOptionalString("4");
  }

  public void testNanoWithAccessorsParseFrom() throws Exception {
    TestNanoAccessors msg = null;
    // Test false on creation, after clear and upon empty parse.
    for (int i = 0; i < 3; i++) {
      if (i == 0) {
        msg = new TestNanoAccessors();
      } else if (i == 1) {
        msg.clear();
      } else if (i == 2) {
        msg = TestNanoAccessors.parseFrom(new byte[0]);
      }
      assertFalse(msg.hasOptionalInt32());
      assertFalse(msg.hasOptionalString());
      assertFalse(msg.hasOptionalBytes());
      assertFalse(msg.hasOptionalNestedEnum());
      assertFalse(msg.hasDefaultInt32());
      assertFalse(msg.hasDefaultString());
      assertFalse(msg.hasDefaultBytes());
      assertFalse(msg.hasDefaultFloatNan());
      assertFalse(msg.hasDefaultNestedEnum());
      msg.optionalNestedMessage = new TestNanoAccessors.NestedMessage();
      msg.optionalNestedMessage.setBb(2);
      msg.setOptionalNestedEnum(TestNanoAccessors.BAZ);
      msg.setDefaultInt32(msg.getDefaultInt32());
    }

    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    //System.out.printf("mss=%d result.length=%d\n", msgSerializedSize, result.length);
    assertTrue(msgSerializedSize == 14);
    assertEquals(result.length, msgSerializedSize);

    // Has fields true upon parse.
    TestNanoAccessors newMsg = TestNanoAccessors.parseFrom(result);
    assertEquals(2, newMsg.optionalNestedMessage.getBb());
    assertTrue(newMsg.optionalNestedMessage.hasBb());
    assertEquals(TestNanoAccessors.BAZ, newMsg.getOptionalNestedEnum());
    assertTrue(newMsg.hasOptionalNestedEnum());

    // Has field true on fields with explicit default values from wire.
    assertTrue(newMsg.hasDefaultInt32());
    assertEquals(41, newMsg.getDefaultInt32());
  }

  public void testNanoWithAccessorsPublicFieldTypes() throws Exception {
    TestNanoAccessors msg = new TestNanoAccessors();
    assertNull(msg.optionalNestedMessage);
    assertEquals(0, msg.id);
    assertEquals(0, msg.repeatedNestedEnum.length);

    TestNanoAccessors newMsg = TestNanoAccessors.parseFrom(MessageNano.toByteArray(msg));
    assertNull(newMsg.optionalNestedMessage);
    assertEquals(0, newMsg.id);
    assertEquals(0, newMsg.repeatedNestedEnum.length);

    TestNanoAccessors.NestedMessage nestedMessage = new TestNanoAccessors.NestedMessage();
    nestedMessage.setBb(5);
    newMsg.optionalNestedMessage = nestedMessage;
    newMsg.id = -1;
    newMsg.repeatedNestedEnum = new int[] { TestAllTypesNano.FOO };

    TestNanoAccessors newMsg2 = TestNanoAccessors.parseFrom(MessageNano.toByteArray(newMsg));
    assertEquals(nestedMessage.getBb(), newMsg2.optionalNestedMessage.getBb());
    assertEquals(-1, newMsg2.id);
    assertEquals(TestAllTypesNano.FOO, newMsg2.repeatedNestedEnum[0]);

    newMsg2.optionalNestedMessage = null;
    newMsg2.id = 0;
    newMsg2.repeatedNestedEnum = null;

    TestNanoAccessors newMsg3 = TestNanoAccessors.parseFrom(MessageNano.toByteArray(newMsg2));
    assertNull(newMsg3.optionalNestedMessage);
    assertEquals(0, newMsg3.id);
    assertEquals(0, newMsg3.repeatedNestedEnum.length);
  }

  public void testNanoWithAccessorsSerialize() throws Exception {
    TestNanoAccessors msg = new TestNanoAccessors();
    msg.setOptionalInt32(msg.getOptionalInt32());
    msg.setOptionalString(msg.getOptionalString());
    msg.setOptionalBytes(msg.getOptionalBytes());
    TestNanoAccessors.NestedMessage nestedMessage = new TestNanoAccessors.NestedMessage();
    nestedMessage.setBb(nestedMessage.getBb());
    msg.optionalNestedMessage = nestedMessage;
    msg.setOptionalNestedEnum(msg.getOptionalNestedEnum());
    msg.setDefaultInt32(msg.getDefaultInt32());
    msg.setDefaultString(msg.getDefaultString());
    msg.setDefaultBytes(msg.getDefaultBytes());
    msg.setDefaultFloatNan(msg.getDefaultFloatNan());
    msg.setDefaultNestedEnum(msg.getDefaultNestedEnum());

    byte [] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    assertEquals(result.length, msgSerializedSize);

    // Now deserialize and find that all fields are set and equal to their defaults.
    TestNanoAccessors newMsg = TestNanoAccessors.parseFrom(result);
    assertTrue(newMsg.hasOptionalInt32());
    assertTrue(newMsg.hasOptionalString());
    assertTrue(newMsg.hasOptionalBytes());
    assertTrue(newMsg.optionalNestedMessage.hasBb());
    assertTrue(newMsg.hasOptionalNestedEnum());
    assertTrue(newMsg.hasDefaultInt32());
    assertTrue(newMsg.hasDefaultString());
    assertTrue(newMsg.hasDefaultBytes());
    assertTrue(newMsg.hasDefaultFloatNan());
    assertTrue(newMsg.hasDefaultNestedEnum());
    assertEquals(0, newMsg.getOptionalInt32());
    assertEquals(0, newMsg.getOptionalString().length());
    assertEquals(0, newMsg.getOptionalBytes().length);
    assertEquals(0, newMsg.optionalNestedMessage.getBb());
    assertEquals(TestNanoAccessors.FOO, newMsg.getOptionalNestedEnum());
    assertEquals(41, newMsg.getDefaultInt32());
    assertEquals("hello", newMsg.getDefaultString());
    assertEquals("world", new String(newMsg.getDefaultBytes(), "UTF-8"));
    assertEquals(TestNanoAccessors.BAR, newMsg.getDefaultNestedEnum());
    assertEquals(Float.NaN, newMsg.getDefaultFloatNan());
    assertEquals(0, newMsg.id);
  }

  public void testNanoJavaEnumStyle() throws Exception {
    EnumClassNanos.EnumClassNano msg = new EnumClassNanos.EnumClassNano();
    assertEquals(EnumClassNanos.FileScopeEnum.ONE, msg.one);
    assertEquals(EnumClassNanos.EnumClassNano.MessageScopeEnum.TWO, msg.two);

    EnumClassNanoMultiple msg2 = new EnumClassNanoMultiple();
    assertEquals(FileScopeEnumMultiple.THREE, msg2.three);
    assertEquals(EnumClassNanoMultiple.MessageScopeEnumMultiple.FOUR, msg2.four);
  }

  /**
   * Tests that fields with a default value of NaN are not serialized when
   * set to NaN. This is a special case as NaN != NaN, so normal equality
   * checks don't work.
   */
  public void testNanoNotANumberDefaults() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.defaultDoubleNan = 0;
    msg.defaultFloatNan = 0;
    byte[] result = MessageNano.toByteArray(msg);
    int msgSerializedSize = msg.getSerializedSize();
    assertTrue(msgSerializedSize > 3);

    msg.defaultDoubleNan = Double.NaN;
    msg.defaultFloatNan = Float.NaN;
    result = MessageNano.toByteArray(msg);
    msgSerializedSize = msg.getSerializedSize();
    assertEquals(3, msgSerializedSize);
  }

  /**
   * Test that a bug in skipRawBytes() has been fixed:  if the skip skips
   * exactly up to a limit, this should not break things.
   */
  public void testSkipRawBytesBug() throws Exception {
    byte[] rawBytes = new byte[] { 1, 2 };
    CodedInputByteBufferNano input = CodedInputByteBufferNano.newInstance(rawBytes);

    int limit = input.pushLimit(1);
    input.skipRawBytes(1);
    input.popLimit(limit);
    assertEquals(2, input.readRawByte());
  }

  /**
   * Test that a bug in skipRawBytes() has been fixed:  if the skip skips
   * past the end of a buffer with a limit that has been set past the end of
   * that buffer, this should not break things.
   */
  public void testSkipRawBytesPastEndOfBufferWithLimit() throws Exception {
    byte[] rawBytes = new byte[] { 1, 2, 3, 4, 5 };
    CodedInputByteBufferNano input = CodedInputByteBufferNano.newInstance(rawBytes);

    int limit = input.pushLimit(4);
    // In order to expose the bug we need to read at least one byte to prime the
    // buffer inside the CodedInputStream.
    assertEquals(1, input.readRawByte());
    // Skip to the end of the limit.
    input.skipRawBytes(3);
    assertTrue(input.isAtEnd());
    input.popLimit(limit);
    assertEquals(5, input.readRawByte());
  }

  // Test a smattering of various proto types for printing
  public void testMessageNanoPrinter() {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.optionalInt32 = 14;
    msg.optionalFloat = 42.3f;
    msg.optionalString = "String \"with' both quotes";
    msg.optionalBytes = new byte[] {'"', '\0', 1, 8};
    msg.optionalGroup = new TestAllTypesNano.OptionalGroup();
    msg.optionalGroup.a = 15;
    msg.repeatedInt64 = new long[2];
    msg.repeatedInt64[0] = 1L;
    msg.repeatedInt64[1] = -1L;
    msg.repeatedBytes = new byte[2][];
    msg.repeatedBytes[1] = new byte[] {'h', 'e', 'l', 'l', 'o'};
    msg.repeatedGroup = new TestAllTypesNano.RepeatedGroup[2];
    msg.repeatedGroup[0] = new TestAllTypesNano.RepeatedGroup();
    msg.repeatedGroup[0].a = -27;
    msg.repeatedGroup[1] = new TestAllTypesNano.RepeatedGroup();
    msg.repeatedGroup[1].a = -72;
    msg.optionalNestedMessage = new TestAllTypesNano.NestedMessage();
    msg.optionalNestedMessage.bb = 7;
    msg.repeatedNestedMessage = new TestAllTypesNano.NestedMessage[2];
    msg.repeatedNestedMessage[0] = new TestAllTypesNano.NestedMessage();
    msg.repeatedNestedMessage[0].bb = 77;
    msg.repeatedNestedMessage[1] = new TestAllTypesNano.NestedMessage();
    msg.repeatedNestedMessage[1].bb = 88;
    msg.optionalNestedEnum = TestAllTypesNano.BAZ;
    msg.repeatedNestedEnum = new int[2];
    msg.repeatedNestedEnum[0] = TestAllTypesNano.BAR;
    msg.repeatedNestedEnum[1] = TestAllTypesNano.FOO;
    msg.repeatedStringPiece = new String[] {null, "world"};

    String protoPrint = msg.toString();
    assertTrue(protoPrint.contains("optional_int32: 14"));
    assertTrue(protoPrint.contains("optional_float: 42.3"));
    assertTrue(protoPrint.contains("optional_double: 0.0"));
    assertTrue(protoPrint.contains("optional_string: \"String \\u0022with\\u0027 both quotes\""));
    assertTrue(protoPrint.contains("optional_bytes: \"\\\"\\000\\001\\010\""));
    assertTrue(protoPrint.contains("optional_group <\n  a: 15\n>"));

    assertTrue(protoPrint.contains("repeated_int64: 1\nrepeated_int64: -1"));
    assertFalse(protoPrint.contains("repeated_bytes: \"\"")); // null should be dropped
    assertTrue(protoPrint.contains("repeated_bytes: \"hello\""));
    assertTrue(protoPrint.contains("repeated_group <\n  a: -27\n>\n"
            + "repeated_group <\n  a: -72\n>"));
    assertTrue(protoPrint.contains("optional_nested_message <\n  bb: 7\n>"));
    assertTrue(protoPrint.contains("repeated_nested_message <\n  bb: 77\n>\n"
            + "repeated_nested_message <\n  bb: 88\n>"));
    assertTrue(protoPrint.contains("optional_nested_enum: 3"));
    assertTrue(protoPrint.contains("repeated_nested_enum: 2\nrepeated_nested_enum: 1"));
    assertTrue(protoPrint.contains("default_int32: 41"));
    assertTrue(protoPrint.contains("default_string: \"hello\""));
    assertFalse(protoPrint.contains("repeated_string_piece: \"\""));  // null should be dropped
    assertTrue(protoPrint.contains("repeated_string_piece: \"world\""));
  }

  public void testMessageNanoPrinterAccessors() throws Exception {
    TestNanoAccessors msg = new TestNanoAccessors();
    msg.setOptionalInt32(13);
    msg.setOptionalString("foo");
    msg.setOptionalBytes(new byte[] {'"', '\0', 1, 8});
    msg.optionalNestedMessage = new TestNanoAccessors.NestedMessage();
    msg.optionalNestedMessage.setBb(7);
    msg.setOptionalNestedEnum(TestNanoAccessors.BAZ);
    msg.repeatedInt32 = new int[] { 1, -1 };
    msg.repeatedString = new String[] { "Hello", "world" };
    msg.repeatedBytes = new byte[2][];
    msg.repeatedBytes[1] = new byte[] {'h', 'e', 'l', 'l', 'o'};
    msg.repeatedNestedMessage = new TestNanoAccessors.NestedMessage[2];
    msg.repeatedNestedMessage[0] = new TestNanoAccessors.NestedMessage();
    msg.repeatedNestedMessage[0].setBb(5);
    msg.repeatedNestedMessage[1] = new TestNanoAccessors.NestedMessage();
    msg.repeatedNestedMessage[1].setBb(6);
    msg.repeatedNestedEnum = new int[] { TestNanoAccessors.FOO, TestNanoAccessors.BAR };
    msg.id = 33;

    String protoPrint = msg.toString();
    assertTrue(protoPrint.contains("optional_int32: 13"));
    assertTrue(protoPrint.contains("optional_string: \"foo\""));
    assertTrue(protoPrint.contains("optional_bytes: \"\\\"\\000\\001\\010\""));
    assertTrue(protoPrint.contains("optional_nested_message <\n  bb: 7\n>"));
    assertTrue(protoPrint.contains("optional_nested_enum: 3"));
    assertTrue(protoPrint.contains("repeated_int32: 1\nrepeated_int32: -1"));
    assertTrue(protoPrint.contains("repeated_string: \"Hello\"\nrepeated_string: \"world\""));
    assertFalse(protoPrint.contains("repeated_bytes: \"\"")); // null should be dropped
    assertTrue(protoPrint.contains("repeated_bytes: \"hello\""));
    assertTrue(protoPrint.contains("repeated_nested_message <\n  bb: 5\n>\n"
            + "repeated_nested_message <\n  bb: 6\n>"));
    assertTrue(protoPrint.contains("repeated_nested_enum: 1\nrepeated_nested_enum: 2"));
    assertTrue(protoPrint.contains("id: 33"));
  }

  public void testExtensions() throws Exception {
    Extensions.ExtendableMessage message = new Extensions.ExtendableMessage();
    message.field = 5;
    message.setExtension(Extensions.someString, "Hello World!");
    message.setExtension(Extensions.someBool, true);
    message.setExtension(Extensions.someInt, 42);
    message.setExtension(Extensions.someLong, 124234234234L);
    message.setExtension(Extensions.someFloat, 42.0f);
    message.setExtension(Extensions.someDouble, 422222.0);
    message.setExtension(Extensions.someEnum, Extensions.FIRST_VALUE);
    AnotherMessage another = new AnotherMessage();
    another.string = "Foo";
    another.value = true;
    message.setExtension(Extensions.someMessage, another);

    message.setExtension(Extensions.someRepeatedString, list("a", "bee", "seeya"));
    message.setExtension(Extensions.someRepeatedBool, list(true, false, true));
    message.setExtension(Extensions.someRepeatedInt, list(4, 8, 15, 16, 23, 42));
    message.setExtension(Extensions.someRepeatedLong, list(4L, 8L, 15L, 16L, 23L, 42L));
    message.setExtension(Extensions.someRepeatedFloat, list(1.0f, 3.0f));
    message.setExtension(Extensions.someRepeatedDouble, list(55.133, 3.14159));
    message.setExtension(Extensions.someRepeatedEnum, list(Extensions.FIRST_VALUE,
        Extensions.SECOND_VALUE));
    AnotherMessage second = new AnotherMessage();
    second.string = "Whee";
    second.value = false;
    message.setExtension(Extensions.someRepeatedMessage, list(another, second));

    byte[] data = MessageNano.toByteArray(message);

    Extensions.ExtendableMessage deserialized = Extensions.ExtendableMessage.parseFrom(data);
    assertEquals(5, deserialized.field);
    assertEquals("Hello World!", deserialized.getExtension(Extensions.someString));
    assertEquals(Boolean.TRUE, deserialized.getExtension(Extensions.someBool));
    assertEquals(Integer.valueOf(42), deserialized.getExtension(Extensions.someInt));
    assertEquals(Long.valueOf(124234234234L), deserialized.getExtension(Extensions.someLong));
    assertEquals(Float.valueOf(42.0f), deserialized.getExtension(Extensions.someFloat));
    assertEquals(Double.valueOf(422222.0), deserialized.getExtension(Extensions.someDouble));
    assertEquals(Integer.valueOf(Extensions.FIRST_VALUE),
        deserialized.getExtension(Extensions.someEnum));
    assertEquals(another.string, deserialized.getExtension(Extensions.someMessage).string);
    assertEquals(another.value, deserialized.getExtension(Extensions.someMessage).value);
    assertEquals(list("a", "bee", "seeya"), deserialized.getExtension(Extensions.someRepeatedString));
    assertEquals(list(true, false, true), deserialized.getExtension(Extensions.someRepeatedBool));
    assertEquals(list(4, 8, 15, 16, 23, 42), deserialized.getExtension(Extensions.someRepeatedInt));
    assertEquals(list(4L, 8L, 15L, 16L, 23L, 42L), deserialized.getExtension(Extensions.someRepeatedLong));
    assertEquals(list(1.0f, 3.0f), deserialized.getExtension(Extensions.someRepeatedFloat));
    assertEquals(list(55.133, 3.14159), deserialized.getExtension(Extensions.someRepeatedDouble));
    assertEquals(list(Extensions.FIRST_VALUE,
        Extensions.SECOND_VALUE), deserialized.getExtension(Extensions.someRepeatedEnum));
    assertEquals("Foo", deserialized.getExtension(Extensions.someRepeatedMessage).get(0).string);
    assertEquals(true, deserialized.getExtension(Extensions.someRepeatedMessage).get(0).value);
    assertEquals("Whee", deserialized.getExtension(Extensions.someRepeatedMessage).get(1).string);
    assertEquals(false, deserialized.getExtension(Extensions.someRepeatedMessage).get(1).value);
  }

  public void testUnknownFields() throws Exception {
    // Check that we roundtrip (serialize and deserialize) unrecognized fields.
    AnotherMessage message = new AnotherMessage();
    message.string = "Hello World";
    message.value = false;

    byte[] bytes = MessageNano.toByteArray(message);
    int extraFieldSize = CodedOutputStream.computeStringSize(1001, "This is an unknown field");
    byte[] newBytes = new byte[bytes.length + extraFieldSize];
    System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
    CodedOutputStream.newInstance(newBytes, bytes.length, extraFieldSize).writeString(1001,
        "This is an unknown field");

    // Deserialize with an unknown field.
    AnotherMessage deserialized = AnotherMessage.parseFrom(newBytes);
    byte[] serialized = MessageNano.toByteArray(deserialized);

    assertEquals(newBytes.length, serialized.length);

    // Clear, and make sure it clears everything.
    deserialized.clear();
    assertEquals(0, MessageNano.toByteArray(deserialized).length);
  }

  public void testMergeFrom() throws Exception {
    SimpleMessageNano message = new SimpleMessageNano();
    message.d = 123;
    byte[] bytes = MessageNano.toByteArray(message);

    SimpleMessageNano newMessage = MessageNano.mergeFrom(new SimpleMessageNano(), bytes);
    assertEquals(message.d, newMessage.d);
  }

  public void testJavaKeyword() throws Exception {
    TestAllTypesNano msg = new TestAllTypesNano();
    msg.synchronized_ = 123;
    assertEquals(123, msg.synchronized_);
  }

  public void testReferenceTypesForPrimitives() throws Exception {
    NanoReferenceTypes.TestAllTypesNano message = new NanoReferenceTypes.TestAllTypesNano();

    // Base check - when nothing is set, we serialize nothing.
    assertHasWireData(message, false);

    message.defaultBool = true;
    assertHasWireData(message, true);

    message.defaultBool = false;
    assertHasWireData(message, true);

    message.defaultBool = null;
    assertHasWireData(message, false);

    message.defaultInt32 = 5;
    assertHasWireData(message, true);

    message.defaultInt32 = null;
    assertHasWireData(message, false);

    message.defaultInt64 = 123456L;
    assertHasWireData(message, true);

    message.defaultInt64 = null;
    assertHasWireData(message, false);

    message.defaultFloat = 1f;
    assertHasWireData(message, true);

    message.defaultFloat = null;
    assertHasWireData(message, false);

    message.defaultDouble = 2.1;
    assertHasWireData(message, true);

    message.defaultDouble = null;
    assertHasWireData(message, false);

    message.defaultString = "hello";
    assertHasWireData(message, true);

    message.defaultString = null;
    assertHasWireData(message, false);

    message.defaultBytes = new byte[] { 1, 2, 3 };
    assertHasWireData(message, true);

    message.defaultBytes = null;
    assertHasWireData(message, false);
  }

  public void testHashCodeEquals() throws Exception {
    // Complete equality:
    TestAllTypesNano a = createMessageForHashCodeEqualsTest();
    TestAllTypesNano aEquivalent = createMessageForHashCodeEqualsTest();

    // Null and empty array for repeated fields equality:
    TestAllTypesNano b = createMessageForHashCodeEqualsTest();
    b.repeatedBool = null;
    b.repeatedFloat = new float[0];
    TestAllTypesNano bEquivalent = createMessageForHashCodeEqualsTest();
    bEquivalent.repeatedBool = new boolean[0];
    bEquivalent.repeatedFloat = null;

    // Ref-element-type repeated fields use non-null subsequence equality:
    TestAllTypesNano c = createMessageForHashCodeEqualsTest();
    c.repeatedString = null;
    c.repeatedStringPiece = new String[] {null, "one", null, "two"};
    c.repeatedBytes = new byte[][] {{3, 4}, null};
    TestAllTypesNano cEquivalent = createMessageForHashCodeEqualsTest();
    cEquivalent.repeatedString = new String[3];
    cEquivalent.repeatedStringPiece = new String[] {"one", "two", null};
    cEquivalent.repeatedBytes = new byte[][] {{3, 4}};

    // Complete equality for messages with has fields:
    TestAllTypesNanoHas d = createMessageWithHasForHashCodeEqualsTest();
    TestAllTypesNanoHas dEquivalent = createMessageWithHasForHashCodeEqualsTest();

    // If has-fields exist, fields with the same default values but
    // different has-field values are different.
    TestAllTypesNanoHas e = createMessageWithHasForHashCodeEqualsTest();
    e.optionalInt32++; // make different from d
    e.hasDefaultString = false;
    TestAllTypesNanoHas eDifferent = createMessageWithHasForHashCodeEqualsTest();
    eDifferent.optionalInt32 = e.optionalInt32;
    eDifferent.hasDefaultString = true;

    // Complete equality for messages with accessors:
    TestNanoAccessors f = createMessageWithAccessorsForHashCodeEqualsTest();
    TestNanoAccessors fEquivalent = createMessageWithAccessorsForHashCodeEqualsTest();

    // If using accessors, explicitly setting a field to its default value
    // should make the message different.
    TestNanoAccessors g = createMessageWithAccessorsForHashCodeEqualsTest();
    g.setOptionalInt32(g.getOptionalInt32() + 1); // make different from f
    g.clearDefaultString();
    TestNanoAccessors gDifferent = createMessageWithAccessorsForHashCodeEqualsTest();
    gDifferent.setOptionalInt32(g.getOptionalInt32());
    gDifferent.setDefaultString(g.getDefaultString());

    // Complete equality for reference typed messages:
    NanoReferenceTypes.TestAllTypesNano h = createRefTypedMessageForHashCodeEqualsTest();
    NanoReferenceTypes.TestAllTypesNano hEquivalent = createRefTypedMessageForHashCodeEqualsTest();

    // Inequality of null and default value for reference typed messages:
    NanoReferenceTypes.TestAllTypesNano i = createRefTypedMessageForHashCodeEqualsTest();
    i.optionalInt32 = 1; // make different from h
    i.optionalFloat = null;
    NanoReferenceTypes.TestAllTypesNano iDifferent = createRefTypedMessageForHashCodeEqualsTest();
    iDifferent.optionalInt32 = i.optionalInt32;
    iDifferent.optionalFloat = 0.0f;

    HashMap<MessageNano, String> hashMap = new HashMap<MessageNano, String>();
    hashMap.put(a, "a");
    hashMap.put(b, "b");
    hashMap.put(c, "c");
    hashMap.put(d, "d");
    hashMap.put(e, "e");
    hashMap.put(f, "f");
    hashMap.put(g, "g");
    hashMap.put(h, "h");
    hashMap.put(i, "i");

    assertEquals(9, hashMap.size()); // a-i should be different from each other.

    assertEquals("a", hashMap.get(a));
    assertEquals("a", hashMap.get(aEquivalent));

    assertEquals("b", hashMap.get(b));
    assertEquals("b", hashMap.get(bEquivalent));

    assertEquals("c", hashMap.get(c));
    assertEquals("c", hashMap.get(cEquivalent));

    assertEquals("d", hashMap.get(d));
    assertEquals("d", hashMap.get(dEquivalent));

    assertEquals("e", hashMap.get(e));
    assertNull(hashMap.get(eDifferent));

    assertEquals("f", hashMap.get(f));
    assertEquals("f", hashMap.get(fEquivalent));

    assertEquals("g", hashMap.get(g));
    assertNull(hashMap.get(gDifferent));

    assertEquals("h", hashMap.get(h));
    assertEquals("h", hashMap.get(hEquivalent));

    assertEquals("i", hashMap.get(i));
    assertNull(hashMap.get(iDifferent));
  }

  private TestAllTypesNano createMessageForHashCodeEqualsTest() {
    TestAllTypesNano message = new TestAllTypesNano();
    message.optionalInt32 = 5;
    message.optionalInt64 = 777;
    message.optionalFloat = 1.0f;
    message.optionalDouble = 2.0;
    message.optionalBool = true;
    message.optionalString = "Hello";
    message.optionalBytes = new byte[] { 1, 2, 3 };
    message.optionalNestedMessage = new TestAllTypesNano.NestedMessage();
    message.optionalNestedMessage.bb = 27;
    message.optionalNestedEnum = TestAllTypesNano.BAR;
    message.repeatedInt32 = new int[] { 5, 6, 7, 8 };
    message.repeatedInt64 = new long[] { 27L, 28L, 29L };
    message.repeatedFloat = new float[] { 5.0f, 6.0f };
    message.repeatedDouble = new double[] { 99.1, 22.5 };
    message.repeatedBool = new boolean[] { true, false, true };
    message.repeatedString = new String[] { "One", "Two" };
    message.repeatedBytes = new byte[][] { { 2, 7 }, { 2, 7 } };
    message.repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] {
      message.optionalNestedMessage,
      message.optionalNestedMessage
    };
    message.repeatedNestedEnum = new int[] {
      TestAllTypesNano.BAR,
      TestAllTypesNano.BAZ
    };
    return message;
  }

  private TestAllTypesNanoHas createMessageWithHasForHashCodeEqualsTest() {
    TestAllTypesNanoHas message = new TestAllTypesNanoHas();
    message.optionalInt32 = 5;
    message.optionalString = "Hello";
    message.optionalBytes = new byte[] { 1, 2, 3 };
    message.optionalNestedMessage = new TestAllTypesNanoHas.NestedMessage();
    message.optionalNestedMessage.bb = 27;
    message.optionalNestedEnum = TestAllTypesNano.BAR;
    message.repeatedInt32 = new int[] { 5, 6, 7, 8 };
    message.repeatedString = new String[] { "One", "Two" };
    message.repeatedBytes = new byte[][] { { 2, 7 }, { 2, 7 } };
    message.repeatedNestedMessage = new TestAllTypesNanoHas.NestedMessage[] {
      message.optionalNestedMessage,
      message.optionalNestedMessage
    };
    message.repeatedNestedEnum = new int[] {
      TestAllTypesNano.BAR,
      TestAllTypesNano.BAZ
    };
    return message;
  }

  private TestNanoAccessors createMessageWithAccessorsForHashCodeEqualsTest() {
    TestNanoAccessors message = new TestNanoAccessors()
        .setOptionalInt32(5)
        .setOptionalString("Hello")
        .setOptionalBytes(new byte[] {1, 2, 3})
        .setOptionalNestedEnum(TestNanoAccessors.BAR);
    message.optionalNestedMessage = new TestNanoAccessors.NestedMessage().setBb(27);
    message.repeatedInt32 = new int[] { 5, 6, 7, 8 };
    message.repeatedString = new String[] { "One", "Two" };
    message.repeatedBytes = new byte[][] { { 2, 7 }, { 2, 7 } };
    message.repeatedNestedMessage = new TestNanoAccessors.NestedMessage[] {
      message.optionalNestedMessage,
      message.optionalNestedMessage
    };
    message.repeatedNestedEnum = new int[] {
      TestAllTypesNano.BAR,
      TestAllTypesNano.BAZ
    };
    return message;
  }

  private NanoReferenceTypes.TestAllTypesNano createRefTypedMessageForHashCodeEqualsTest() {
    NanoReferenceTypes.TestAllTypesNano message = new NanoReferenceTypes.TestAllTypesNano();
    message.optionalInt32 = 5;
    message.optionalInt64 = 777L;
    message.optionalFloat = 1.0f;
    message.optionalDouble = 2.0;
    message.optionalBool = true;
    message.optionalString = "Hello";
    message.optionalBytes = new byte[] { 1, 2, 3 };
    message.optionalNestedMessage =
        new NanoReferenceTypes.TestAllTypesNano.NestedMessage();
    message.optionalNestedMessage.foo = 27;
    message.optionalNestedEnum = NanoReferenceTypes.TestAllTypesNano.BAR;
    message.repeatedInt32 = new int[] { 5, 6, 7, 8 };
    message.repeatedInt64 = new long[] { 27L, 28L, 29L };
    message.repeatedFloat = new float[] { 5.0f, 6.0f };
    message.repeatedDouble = new double[] { 99.1, 22.5 };
    message.repeatedBool = new boolean[] { true, false, true };
    message.repeatedString = new String[] { "One", "Two" };
    message.repeatedBytes = new byte[][] { { 2, 7 }, { 2, 7 } };
    message.repeatedNestedMessage =
        new NanoReferenceTypes.TestAllTypesNano.NestedMessage[] {
          message.optionalNestedMessage,
          message.optionalNestedMessage
        };
    message.repeatedNestedEnum = new int[] {
      NanoReferenceTypes.TestAllTypesNano.BAR,
      NanoReferenceTypes.TestAllTypesNano.BAZ
    };
    return message;
  }

  public void testEqualsWithSpecialFloatingPointValues() throws Exception {
    // Checks that the nano implementation complies with Object.equals() when treating
    // floating point numbers, i.e. NaN == NaN and +0.0 != -0.0.
    // This test assumes that the generated equals() implementations are symmetric, so
    // there will only be one direction for each equality check.

    TestAllTypesNano m1 = new TestAllTypesNano();
    m1.optionalFloat = Float.NaN;
    m1.optionalDouble = Double.NaN;
    TestAllTypesNano m2 = new TestAllTypesNano();
    m2.optionalFloat = Float.NaN;
    m2.optionalDouble = Double.NaN;
    assertTrue(m1.equals(m2));
    assertTrue(m1.equals(
        MessageNano.mergeFrom(new TestAllTypesNano(), MessageNano.toByteArray(m1))));

    m1.optionalFloat = +0f;
    m2.optionalFloat = -0f;
    assertFalse(m1.equals(m2));

    m1.optionalFloat = -0f;
    m1.optionalDouble = +0d;
    m2.optionalDouble = -0d;
    assertFalse(m1.equals(m2));

    m1.optionalDouble = -0d;
    assertTrue(m1.equals(m2));
    assertFalse(m1.equals(new TestAllTypesNano())); // -0 does not equals() the default +0
    assertTrue(m1.equals(
        MessageNano.mergeFrom(new TestAllTypesNano(), MessageNano.toByteArray(m1))));

    // -------

    TestAllTypesNanoHas m3 = new TestAllTypesNanoHas();
    m3.optionalFloat = Float.NaN;
    m3.hasOptionalFloat = true;
    m3.optionalDouble = Double.NaN;
    m3.hasOptionalDouble = true;
    TestAllTypesNanoHas m4 = new TestAllTypesNanoHas();
    m4.optionalFloat = Float.NaN;
    m4.hasOptionalFloat = true;
    m4.optionalDouble = Double.NaN;
    m4.hasOptionalDouble = true;
    assertTrue(m3.equals(m4));
    assertTrue(m3.equals(
        MessageNano.mergeFrom(new TestAllTypesNanoHas(), MessageNano.toByteArray(m3))));

    m3.optionalFloat = +0f;
    m4.optionalFloat = -0f;
    assertFalse(m3.equals(m4));

    m3.optionalFloat = -0f;
    m3.optionalDouble = +0d;
    m4.optionalDouble = -0d;
    assertFalse(m3.equals(m4));

    m3.optionalDouble = -0d;
    m3.hasOptionalFloat = false;  // -0 does not equals() the default +0,
    m3.hasOptionalDouble = false; // so these incorrect 'has' flags should be disregarded.
    assertTrue(m3.equals(m4));    // note: m4 has the 'has' flags set.
    assertFalse(m3.equals(new TestAllTypesNanoHas())); // note: the new message has +0 defaults
    assertTrue(m3.equals(
        MessageNano.mergeFrom(new TestAllTypesNanoHas(), MessageNano.toByteArray(m3))));
                                  // note: the deserialized message has the 'has' flags set.

    // -------

    TestNanoAccessors m5 = new TestNanoAccessors();
    m5.setOptionalFloat(Float.NaN);
    m5.setOptionalDouble(Double.NaN);
    TestNanoAccessors m6 = new TestNanoAccessors();
    m6.setOptionalFloat(Float.NaN);
    m6.setOptionalDouble(Double.NaN);
    assertTrue(m5.equals(m6));
    assertTrue(m5.equals(
        MessageNano.mergeFrom(new TestNanoAccessors(), MessageNano.toByteArray(m6))));

    m5.setOptionalFloat(+0f);
    m6.setOptionalFloat(-0f);
    assertFalse(m5.equals(m6));

    m5.setOptionalFloat(-0f);
    m5.setOptionalDouble(+0d);
    m6.setOptionalDouble(-0d);
    assertFalse(m5.equals(m6));

    m5.setOptionalDouble(-0d);
    assertTrue(m5.equals(m6));
    assertFalse(m5.equals(new TestNanoAccessors()));
    assertTrue(m5.equals(
        MessageNano.mergeFrom(new TestNanoAccessors(), MessageNano.toByteArray(m6))));

    // -------

    NanoReferenceTypes.TestAllTypesNano m7 = new NanoReferenceTypes.TestAllTypesNano();
    m7.optionalFloat = Float.NaN;
    m7.optionalDouble = Double.NaN;
    NanoReferenceTypes.TestAllTypesNano m8 = new NanoReferenceTypes.TestAllTypesNano();
    m8.optionalFloat = Float.NaN;
    m8.optionalDouble = Double.NaN;
    assertTrue(m7.equals(m8));
    assertTrue(m7.equals(MessageNano.mergeFrom(
        new NanoReferenceTypes.TestAllTypesNano(), MessageNano.toByteArray(m7))));

    m7.optionalFloat = +0f;
    m8.optionalFloat = -0f;
    assertFalse(m7.equals(m8));

    m7.optionalFloat = -0f;
    m7.optionalDouble = +0d;
    m8.optionalDouble = -0d;
    assertFalse(m7.equals(m8));

    m7.optionalDouble = -0d;
    assertTrue(m7.equals(m8));
    assertFalse(m7.equals(new NanoReferenceTypes.TestAllTypesNano()));
    assertTrue(m7.equals(MessageNano.mergeFrom(
        new NanoReferenceTypes.TestAllTypesNano(), MessageNano.toByteArray(m7))));
  }

  public void testNullRepeatedFields() throws Exception {
    // Check that serialization after explicitly setting a repeated field
    // to null doesn't NPE.
    TestAllTypesNano message = new TestAllTypesNano();
    message.repeatedInt32 = null;
    MessageNano.toByteArray(message);  // should not NPE
    message.toString(); // should not NPE

    message.repeatedNestedEnum = null;
    MessageNano.toByteArray(message);  // should not NPE
    message.toString(); // should not NPE

    message.repeatedBytes = null;
    MessageNano.toByteArray(message); // should not NPE
    message.toString(); // should not NPE

    message.repeatedNestedMessage = null;
    MessageNano.toByteArray(message); // should not NPE
    message.toString(); // should not NPE

    message.repeatedPackedInt32 = null;
    MessageNano.toByteArray(message); // should not NPE
    message.toString(); // should not NPE

    message.repeatedPackedNestedEnum = null;
    MessageNano.toByteArray(message); // should not NPE
    message.toString(); // should not NPE

    // Create a second message to merge into message.
    TestAllTypesNano secondMessage = new TestAllTypesNano();
    secondMessage.repeatedInt32 = new int[] {1, 2, 3};
    secondMessage.repeatedNestedEnum = new int[] {
      TestAllTypesNano.FOO, TestAllTypesNano.BAR
    };
    secondMessage.repeatedBytes = new byte[][] {{1, 2}, {3, 4}};
    TestAllTypesNano.NestedMessage nested =
        new TestAllTypesNano.NestedMessage();
    nested.bb = 55;
    secondMessage.repeatedNestedMessage =
        new TestAllTypesNano.NestedMessage[] {nested};
    secondMessage.repeatedPackedInt32 = new int[] {1, 2, 3};
    secondMessage.repeatedPackedNestedEnum = new int[] {
        TestAllTypesNano.FOO, TestAllTypesNano.BAR
      };

    // Should not NPE
    message.mergeFrom(CodedInputByteBufferNano.newInstance(
        MessageNano.toByteArray(secondMessage)));
    assertEquals(3, message.repeatedInt32.length);
    assertEquals(3, message.repeatedInt32[2]);
    assertEquals(2, message.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.FOO, message.repeatedNestedEnum[0]);
    assertEquals(2, message.repeatedBytes.length);
    assertEquals(4, message.repeatedBytes[1][1]);
    assertEquals(1, message.repeatedNestedMessage.length);
    assertEquals(55, message.repeatedNestedMessage[0].bb);
    assertEquals(3, message.repeatedPackedInt32.length);
    assertEquals(2, message.repeatedPackedInt32[1]);
    assertEquals(2, message.repeatedPackedNestedEnum.length);
    assertEquals(TestAllTypesNano.BAR, message.repeatedPackedNestedEnum[1]);
  }

  public void testNullRepeatedFieldElements() throws Exception {
    // Check that serialization with null array elements doesn't NPE.
    String string1 = "1";
    String string2 = "2";
    byte[] bytes1 = {3, 4};
    byte[] bytes2 = {5, 6};
    TestAllTypesNano.NestedMessage msg1 = new TestAllTypesNano.NestedMessage();
    msg1.bb = 7;
    TestAllTypesNano.NestedMessage msg2 = new TestAllTypesNano.NestedMessage();
    msg2.bb = 8;

    TestAllTypesNano message = new TestAllTypesNano();
    message.repeatedString = new String[] {null, string1, string2};
    message.repeatedBytes = new byte[][] {bytes1, null, bytes2};
    message.repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] {msg1, msg2, null};
    message.repeatedGroup = new TestAllTypesNano.RepeatedGroup[] {null, null, null};

    byte[] serialized = MessageNano.toByteArray(message); // should not NPE
    TestAllTypesNano deserialized = MessageNano.mergeFrom(new TestAllTypesNano(), serialized);
    assertEquals(2, deserialized.repeatedString.length);
    assertEquals(string1, deserialized.repeatedString[0]);
    assertEquals(string2, deserialized.repeatedString[1]);
    assertEquals(2, deserialized.repeatedBytes.length);
    assertTrue(Arrays.equals(bytes1, deserialized.repeatedBytes[0]));
    assertTrue(Arrays.equals(bytes2, deserialized.repeatedBytes[1]));
    assertEquals(2, deserialized.repeatedNestedMessage.length);
    assertEquals(msg1.bb, deserialized.repeatedNestedMessage[0].bb);
    assertEquals(msg2.bb, deserialized.repeatedNestedMessage[1].bb);
    assertEquals(0, deserialized.repeatedGroup.length);
  }

  public void testRepeatedMerge() throws Exception {
    // Check that merging repeated fields cause the arrays to expand with
    // new data.
    TestAllTypesNano first = new TestAllTypesNano();
    first.repeatedInt32 = new int[] {1, 2, 3};
    TestAllTypesNano second = new TestAllTypesNano();
    second.repeatedInt32 = new int[] {4, 5};
    MessageNano.mergeFrom(first, MessageNano.toByteArray(second));
    assertEquals(5, first.repeatedInt32.length);
    assertEquals(1, first.repeatedInt32[0]);
    assertEquals(4, first.repeatedInt32[3]);

    first = new TestAllTypesNano();
    first.repeatedNestedEnum = new int[] {TestAllTypesNano.BAR};
    second = new TestAllTypesNano();
    second.repeatedNestedEnum = new int[] {TestAllTypesNano.FOO};
    MessageNano.mergeFrom(first, MessageNano.toByteArray(second));
    assertEquals(2, first.repeatedNestedEnum.length);
    assertEquals(TestAllTypesNano.BAR, first.repeatedNestedEnum[0]);
    assertEquals(TestAllTypesNano.FOO, first.repeatedNestedEnum[1]);

    first = new TestAllTypesNano();
    first.repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] {
      new TestAllTypesNano.NestedMessage()
    };
    first.repeatedNestedMessage[0].bb = 3;
    second = new TestAllTypesNano();
    second.repeatedNestedMessage = new TestAllTypesNano.NestedMessage[] {
      new TestAllTypesNano.NestedMessage()
    };
    second.repeatedNestedMessage[0].bb = 5;
    MessageNano.mergeFrom(first, MessageNano.toByteArray(second));
    assertEquals(2, first.repeatedNestedMessage.length);
    assertEquals(3, first.repeatedNestedMessage[0].bb);
    assertEquals(5, first.repeatedNestedMessage[1].bb);

    first = new TestAllTypesNano();
    first.repeatedPackedSfixed64 = new long[] {-1, -2, -3};
    second = new TestAllTypesNano();
    second.repeatedPackedSfixed64 = new long[] {-4, -5};
    MessageNano.mergeFrom(first, MessageNano.toByteArray(second));
    assertEquals(5, first.repeatedPackedSfixed64.length);
    assertEquals(-1, first.repeatedPackedSfixed64[0]);
    assertEquals(-4, first.repeatedPackedSfixed64[3]);

    first = new TestAllTypesNano();
    first.repeatedPackedNestedEnum = new int[] {TestAllTypesNano.BAR};
    second = new TestAllTypesNano();
    second.repeatedPackedNestedEnum = new int[] {TestAllTypesNano.FOO};
    MessageNano.mergeFrom(first, MessageNano.toByteArray(second));
    assertEquals(2, first.repeatedPackedNestedEnum.length);
    assertEquals(TestAllTypesNano.BAR, first.repeatedPackedNestedEnum[0]);
    assertEquals(TestAllTypesNano.FOO, first.repeatedPackedNestedEnum[1]);

    // Now test repeated merging in a nested scope
    TestRepeatedMergeNano firstContainer = new TestRepeatedMergeNano();
    firstContainer.contained = new TestAllTypesNano();
    firstContainer.contained.repeatedInt32 = new int[] {10, 20};
    TestRepeatedMergeNano secondContainer = new TestRepeatedMergeNano();
    secondContainer.contained = new TestAllTypesNano();
    secondContainer.contained.repeatedInt32 = new int[] {30};
    MessageNano.mergeFrom(firstContainer, MessageNano.toByteArray(secondContainer));
    assertEquals(3, firstContainer.contained.repeatedInt32.length);
    assertEquals(20, firstContainer.contained.repeatedInt32[1]);
    assertEquals(30, firstContainer.contained.repeatedInt32[2]);
  }

  public void testRepeatedPackables() throws Exception {
    // Check that repeated fields with packable types can accept both packed and unpacked
    // serialized forms.
    NanoRepeatedPackables.NonPacked nonPacked = new NanoRepeatedPackables.NonPacked();
    // Exaggerates the first values of varint-typed arrays. This is to test that the parsing code
    // of packed fields handles non-packed data correctly. If the code incorrectly thinks it is
    // reading from a packed tag, it will read the first value as the byte length of the field,
    // and the large number will cause the input to go out of bounds, thus capturing the error.
    nonPacked.int32S = new int[] {1000, 2, 3};
    nonPacked.int64S = new long[] {4000, 5, 6};
    nonPacked.uint32S = new int[] {7000, 8, 9};
    nonPacked.uint64S = new long[] {10000, 11, 12};
    nonPacked.sint32S = new int[] {13000, 14, 15};
    nonPacked.sint64S = new long[] {16000, 17, 18};
    nonPacked.fixed32S = new int[] {19, 20, 21};
    nonPacked.fixed64S = new long[] {22, 23, 24};
    nonPacked.sfixed32S = new int[] {25, 26, 27};
    nonPacked.sfixed64S = new long[] {28, 29, 30};
    nonPacked.floats = new float[] {31, 32, 33};
    nonPacked.doubles = new double[] {34, 35, 36};
    nonPacked.bools = new boolean[] {false, true};
    nonPacked.enums = new int[] {
      NanoRepeatedPackables.Enum.OPTION_ONE,
      NanoRepeatedPackables.Enum.OPTION_TWO,
    };
    nonPacked.noise = 13579;

    byte[] nonPackedSerialized = MessageNano.toByteArray(nonPacked);

    NanoRepeatedPackables.Packed packed =
        MessageNano.mergeFrom(new NanoRepeatedPackables.Packed(), nonPackedSerialized);
    assertRepeatedPackablesEqual(nonPacked, packed);

    byte[] packedSerialized = MessageNano.toByteArray(packed);
    // Just a cautious check that the two serialized forms are different,
    // to make sure the remaining of this test is useful:
    assertFalse(Arrays.equals(nonPackedSerialized, packedSerialized));

    nonPacked = MessageNano.mergeFrom(new NanoRepeatedPackables.NonPacked(), packedSerialized);
    assertRepeatedPackablesEqual(nonPacked, packed);

    // Test mixed serialized form.
    byte[] mixedSerialized = new byte[nonPackedSerialized.length + packedSerialized.length];
    System.arraycopy(nonPackedSerialized, 0, mixedSerialized, 0, nonPackedSerialized.length);
    System.arraycopy(packedSerialized, 0,
        mixedSerialized, nonPackedSerialized.length, packedSerialized.length);

    nonPacked = MessageNano.mergeFrom(new NanoRepeatedPackables.NonPacked(), mixedSerialized);
    packed = MessageNano.mergeFrom(new NanoRepeatedPackables.Packed(), mixedSerialized);
    assertRepeatedPackablesEqual(nonPacked, packed);
    assertTrue(Arrays.equals(new int[] {1000, 2, 3, 1000, 2, 3}, nonPacked.int32S));
    assertTrue(Arrays.equals(new int[] {13000, 14, 15, 13000, 14, 15}, nonPacked.sint32S));
    assertTrue(Arrays.equals(new int[] {25, 26, 27, 25, 26, 27}, nonPacked.sfixed32S));
    assertTrue(Arrays.equals(new boolean[] {false, true, false, true}, nonPacked.bools));
  }

  private void assertRepeatedPackablesEqual(
      NanoRepeatedPackables.NonPacked nonPacked, NanoRepeatedPackables.Packed packed) {
    // Not using MessageNano.equals() -- that belongs to a separate test.
    assertTrue(Arrays.equals(nonPacked.int32S, packed.int32S));
    assertTrue(Arrays.equals(nonPacked.int64S, packed.int64S));
    assertTrue(Arrays.equals(nonPacked.uint32S, packed.uint32S));
    assertTrue(Arrays.equals(nonPacked.uint64S, packed.uint64S));
    assertTrue(Arrays.equals(nonPacked.sint32S, packed.sint32S));
    assertTrue(Arrays.equals(nonPacked.sint64S, packed.sint64S));
    assertTrue(Arrays.equals(nonPacked.fixed32S, packed.fixed32S));
    assertTrue(Arrays.equals(nonPacked.fixed64S, packed.fixed64S));
    assertTrue(Arrays.equals(nonPacked.sfixed32S, packed.sfixed32S));
    assertTrue(Arrays.equals(nonPacked.sfixed64S, packed.sfixed64S));
    assertTrue(Arrays.equals(nonPacked.floats, packed.floats));
    assertTrue(Arrays.equals(nonPacked.doubles, packed.doubles));
    assertTrue(Arrays.equals(nonPacked.bools, packed.bools));
    assertTrue(Arrays.equals(nonPacked.enums, packed.enums));
  }

  private void assertHasWireData(MessageNano message, boolean expected) {
    byte[] bytes = MessageNano.toByteArray(message);
    int wireLength = bytes.length;
    if (expected) {
      assertFalse(wireLength == 0);
    } else {
      if (wireLength != 0) {
        fail("Expected no wire data for message \n" + message
            + "\nBut got:\n"
            + hexDump(bytes));
      }
    }
  }

  private static String hexDump(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x ", b));
    }
    return sb.toString();
  }

  private <T> List<T> list(T first, T... remaining) {
    List<T> list = new ArrayList<T>();
    list.add(first);
    for (T item : remaining) {
      list.add(item);
    }
    return list;
  }
}
